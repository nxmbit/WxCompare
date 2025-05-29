package com.nxmbit.wxcompare.controller;

import com.google.common.eventbus.Subscribe;
import com.nxmbit.wxcompare.enums.WeatherSortField;
import com.nxmbit.wxcompare.event.WeatherDataUpdatedEvent;
import com.nxmbit.wxcompare.model.Location;
import com.nxmbit.wxcompare.model.User;
import com.nxmbit.wxcompare.model.Weather;
import com.nxmbit.wxcompare.repository.LocationRepository;
import com.nxmbit.wxcompare.repository.UserRepository;
import com.nxmbit.wxcompare.service.EventBusService;
import com.nxmbit.wxcompare.service.WeatherDataManager;
import com.nxmbit.wxcompare.util.ControllerUtils;
import com.nxmbit.wxcompare.util.WeatherIconUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class LocationsListController implements Initializable {
    @FXML
    private VBox locationsContainer;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Label noLocationsLabel;

    // paginaton controls
    @FXML
    private HBox paginationBox;
    @FXML
    private Button prevPageButton;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Button nextPageButton;
    @FXML
    private ComboBox<Integer> itemsPerPageCombo;

    // sorting controls
    @FXML
    private HBox sortControlsBox;
    @FXML
    private ChoiceBox<WeatherSortField> sortByChoiceBox;
    @FXML
    private ToggleButton sortOrderToggle;

    private final LocationRepository locationRepository = new LocationRepository();
    private final UserRepository userRepository = new UserRepository();
    private final WeatherDataManager weatherDataManager = WeatherDataManager.getInstance();

    // pagination and sorting variables
    private List<Location> allLocations = new ArrayList<>();
    private final Map<Long, Weather> locationWeatherMap = new ConcurrentHashMap<>();
    private int currentPage = 0;
    private int locationsPerPage = 5;
    private WeatherSortField sortBy = WeatherSortField.NAME;
    private boolean sortAscending = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        EventBusService.register(this);
        setupPaginationControls();
        setupSortControls();
        loadLocations();
    }

    private void setupPaginationControls() {
        // Initialize items per page combo
        itemsPerPageCombo.getItems().addAll(5, 10, 15, 20, 50);
        itemsPerPageCombo.setValue(locationsPerPage);

        // Configure pagination controls
        prevPageButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                displayCurrentPage();
            }
        });

        nextPageButton.setOnAction(e -> {
            int totalPages = (int) Math.ceil((double) allLocations.size() / locationsPerPage);
            if (currentPage < totalPages - 1) {
                currentPage++;
                displayCurrentPage();
            }
        });

        itemsPerPageCombo.setOnAction(e -> {
            locationsPerPage = itemsPerPageCombo.getValue();
            currentPage = 0;
            displayCurrentPage();
        });
    }

    private void setupSortControls() {
        // Configure sort choice box
        sortByChoiceBox.setItems(FXCollections.observableArrayList(WeatherSortField.values()));
        sortByChoiceBox.setValue(WeatherSortField.NAME);  // Default sort

        sortByChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                sortBy = newVal;
                sortAndDisplayLocations();
            }
        });

        // Configure sort order toggle
        sortOrderToggle.setSelected(false); // Start with ascending
        sortOrderToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            sortAscending = !newVal;
            sortOrderToggle.setText(sortAscending ? "Ascending" : "Descending");
            sortAndDisplayLocations();
        });
    }

    @FXML
    private void openLocationSearch() {
        ControllerUtils.loadView("locations-search-view.fxml");
    }

    private void loadLocations() {
        locationsContainer.getChildren().clear();
        loadingIndicator.setVisible(true);
        noLocationsLabel.setVisible(false);
        paginationBox.setVisible(false);
        sortControlsBox.setVisible(false);
        currentPage = 0;

        CompletableFuture.supplyAsync(locationRepository::findAll)
                .thenAccept(locations -> {
                    Platform.runLater(() -> {
                        if (locations.isEmpty()) {
                            loadingIndicator.setVisible(false);
                            noLocationsLabel.setVisible(true);
                            return;
                        }

                        loadingIndicator.setVisible(false);
                        paginationBox.setVisible(true);
                        sortControlsBox.setVisible(true);
                        allLocations = new ArrayList<>(locations);
                        loadWeatherDataForLocations(locations);
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        showAlert("Error", "Could not load locations: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                    return null;
                });
    }

    private void loadWeatherDataForLocations(List<Location> locations) {
        locationWeatherMap.clear();

        // array of futures, one for each location
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Location location : locations) {
            CompletableFuture<Void> future = weatherDataManager.getWeatherForLocation(location)
                    .thenAccept(weather -> locationWeatherMap.put(location.getId(), weather));

            futures.add(future);
        }

        // update ui when all futures are complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> Platform.runLater(this::sortAndDisplayLocations));
    }

    @FXML
    private void refreshWeatherData() {
        weatherDataManager.refreshAllWeatherData()
                .thenRun(this::loadLocations);
    }

    private void sortAndDisplayLocations() {
        List<Location> sortedLocations = new ArrayList<>(allLocations);

        if (sortBy == WeatherSortField.NAME) {
            sortedLocations.sort((loc1, loc2) -> {
                int result = loc1.getName().compareToIgnoreCase(loc2.getName());
                return sortAscending ? result : -result;
            });
        } else {
            sortedLocations.sort((loc1, loc2) -> {
                Weather weather1 = locationWeatherMap.get(loc1.getId());
                Weather weather2 = locationWeatherMap.get(loc2.getId());

                // If weather data is missing, put at the end
                if (weather1 == null) return (sortAscending ? 1 : -1);
                if (weather2 == null) return (sortAscending ? -1 : 1);

                int result = switch (sortBy) {
                    case TEMPERATURE -> Double.compare(weather1.getTemperature(), weather2.getTemperature());
                    case HUMIDITY -> Integer.compare(weather1.getHumidity(), weather2.getHumidity());
                    case PRESSURE -> Integer.compare(weather1.getPressure(), weather2.getPressure());
                    case WIND_SPEED -> Double.compare(weather1.getWindSpeed(), weather2.getWindSpeed());
                    case CLOUDINESS -> Integer.compare(weather1.getCloudinessPercent(), weather2.getCloudinessPercent());
                    case FEELS_LIKE -> Double.compare(weather1.getFeelsLike(), weather2.getFeelsLike());
                    case VISIBILITY -> Integer.compare(weather1.getVisibilityInMeters(), weather2.getVisibilityInMeters());
                    case RAIN_1H -> Integer.compare(weather1.getRainMmLastHour(), weather2.getRainMmLastHour());
                    case SNOW_1H -> Integer.compare(weather1.getSnowMmLastHour(), weather2.getSnowMmLastHour());
                    default -> 0;
                };

                return (sortAscending ? result : -result);
            });
        }

        // update ui
        allLocations = sortedLocations;
        displayCurrentPage();
    }

    private void displayCurrentPage() {
        locationsContainer.getChildren().clear();

        int startIndex = currentPage * locationsPerPage;
        int endIndex = Math.min(startIndex + locationsPerPage, allLocations.size());

        // Update page info
        int totalPages = (int) Math.ceil((double) allLocations.size() / locationsPerPage);
        pageInfoLabel.setText(String.format("Page %d of %d", currentPage + 1, Math.max(1, totalPages)));

        prevPageButton.setDisable(currentPage <= 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);

        // No locations on this page
        if (startIndex >= allLocations.size()) {
            noLocationsLabel.setVisible(true);
            return;
        }

        noLocationsLabel.setVisible(false);

        // Display locations for current page
        List<Location> pageLocations = allLocations.subList(startIndex, endIndex);
        User user = userRepository.findFirstUser().orElse(new User());

        for (Location location : pageLocations) {
            VBox locationCard = createLocationCard(location);
            locationsContainer.getChildren().add(locationCard);

            Weather weather = locationWeatherMap.get(location.getId());
            if (weather != null) {
                updateLocationCardWithWeather(locationCard, weather, user, location);
            } else {
                // Show loading or error state
                ProgressIndicator weatherLoading = new ProgressIndicator();
                weatherLoading.setPrefSize(24, 24);
                locationCard.getChildren().add(weatherLoading);
            }
        }
    }

    private VBox createLocationCard(Location location) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 5; -fx-padding: 15;");
        card.setUserData(location.getId());

        Label nameLabel = new Label(location.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ProgressIndicator weatherLoading = new ProgressIndicator();
        weatherLoading.setPrefSize(24, 24);

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Hyperlink detailsLink = new Hyperlink("Show Details ▼");

        topRow.getChildren().addAll(nameLabel, spacer, detailsLink);

        card.getChildren().addAll(topRow, weatherLoading);

        return card;
    }

    private void updateLocationCardWithWeather(VBox locationCard, Weather weather, User user, Location location) {
        locationCard.getChildren().removeIf(node -> node instanceof ProgressIndicator);

        HBox topRow = (HBox) locationCard.getChildren().get(0);
        Hyperlink detailsLink = (Hyperlink) topRow.getChildren().get(2);

        HBox mainContentArea = new HBox(20);
        mainContentArea.setAlignment(Pos.CENTER_LEFT);

        HBox leftSide = new HBox(10);
        leftSide.setAlignment(Pos.CENTER_LEFT);

        // Weather icon
        ImageView weatherIcon = WeatherIconUtil.createWeatherIcon(weather.getIconCode(), 64);

        // Temperature and description
        VBox tempBox = new VBox(5);

        String unitSymbol = " °K";
        if (user.getTemperatureUnit() != null) {
            unitSymbol = switch (user.getTemperatureUnit()) {
                case FAHRENHEIT -> " °F";
                case KELVIN -> " °K";
                case CELSIUS -> " °C";
            };
        }

        Label tempLabel = new Label(String.format("%.1f%s", weather.getTemperature(), unitSymbol));
        tempLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label descLabel = new Label(weather.getDescription());
        descLabel.setStyle("-fx-font-size: 14px;");

        tempBox.getChildren().addAll(tempLabel, descLabel);
        leftSide.getChildren().addAll(weatherIcon, tempBox);

        VBox rightSide = new VBox(5);
        rightSide.setAlignment(Pos.CENTER_LEFT);

        Label humidityLabel = new Label(String.format("Humidity: %d%%", weather.getHumidity()));
        Label pressureLabel = new Label(String.format("Pressure: %d hPa", weather.getPressure()));

        String windSpeedUnit = user.getSystemOfMeasurement() == null ? "m/s" :
                user.getSystemOfMeasurement().toString().equals("METRIC") ? "m/s" : "mph";

        Label windLabel = new Label(String.format("Wind: %.1f %s", weather.getWindSpeed(), windSpeedUnit));

        rightSide.getChildren().addAll(humidityLabel, pressureLabel, windLabel);

        mainContentArea.getChildren().addAll(leftSide, rightSide);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, dd MMM");
        LocalDateTime dateTime = LocalDateTime.ofInstant(weather.getTimestamp(), ZoneId.systemDefault());
        Label updatedLabel = new Label("Updated: " + formatter.format(dateTime));
        updatedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        locationCard.getChildren().addAll(mainContentArea, updatedLabel);

        // dropdown
        VBox detailsBox = createDetailsBox(weather, user, location);
        detailsBox.setVisible(false);
        detailsBox.setManaged(false);
        locationCard.getChildren().add(detailsBox);

        detailsLink.setOnAction(e -> {
            boolean isVisible = detailsBox.isVisible();
            detailsBox.setVisible(!isVisible);
            detailsBox.setManaged(!isVisible);
            detailsLink.setText(isVisible ? "Show Details ▼" : "Hide Details ▲");
        });
    }

    private VBox createDetailsBox(Weather weather, User user, Location location) {
        VBox detailsBox = new VBox(10);
        detailsBox.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(8);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (int i = 0; i < 9; i++) {
            ColumnConstraints col = new ColumnConstraints();
            // Make every third column (spacer) wider
            if (i % 3 == 2) {
                col.setPrefWidth(40);
            }
            detailsGrid.getColumnConstraints().add(col);
        }

        String unitSymbol = user.getTemperatureUnit() != null ?
                switch (user.getTemperatureUnit()) {
                    case FAHRENHEIT -> "°F";
                    case KELVIN -> "°K";
                    case CELSIUS -> "°C";
                } : "°K";

        String[][] gridData = {
                {"Feels Like:", String.format("%.1f %s", weather.getFeelsLike(), unitSymbol)},
                {"Min Temp:", String.format("%.1f %s", weather.getTempMin(), unitSymbol)},
                {"Max Temp:", String.format("%.1f %s", weather.getTempMax(), unitSymbol)},
                {"Wind Direction:", getWindDirection(weather.getWindDeg())},
                {"Cloudiness:", weather.getCloudinessPercent() + "%"},
                {"Visibility:", formatVisibility(weather.getVisibilityInMeters(), user)},
                {"Rain (last hr):", weather.getRainMmLastHour() + " mm"},
                {"Snow (last hr):", weather.getSnowMmLastHour() + " mm"},
                {"Sunrise:", timeFormatter.format(LocalDateTime.ofInstant(weather.getSunrise(), ZoneId.systemDefault()))},
                {"Sunset:", timeFormatter.format(LocalDateTime.ofInstant(weather.getSunset(), ZoneId.systemDefault()))}
        };

        // Add items to grid with spacer columns between groups
        for (int i = 0; i < gridData.length; i++) {
            int groupIndex = i % 3;            // Position within group (0, 1, or 2)
            int groupRow = i / 3;              // Row in the grid
            int baseCol = groupIndex * 3;      // Start with wider spacing (3 columns per item)

            detailsGrid.add(new Label(gridData[i][0]), baseCol, groupRow);
            detailsGrid.add(new Label(gridData[i][1]), baseCol + 1, groupRow);
            // Column baseCol + 2 is empty and serves as a spacer
        }

        Button removeButton = new Button("Remove Location");
        removeButton.setOnAction(event -> removeLocation(location));
        detailsBox.getChildren().addAll(detailsGrid, removeButton);

        return detailsBox;
    }

    private String getWindDirection(int degrees) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        return directions[(int)Math.round(degrees % 360 / 45.0)] + " (" + degrees + "°)";
    }

    private String formatVisibility(int meters, User user) {
        if (user.getSystemOfMeasurement() != null &&
                user.getSystemOfMeasurement().toString().equals("IMPERIAL")) {
            double miles = meters / 1609.34;
            return String.format("%.1f mi", miles);
        } else {
            if (meters >= 1000) {
                return String.format("%.1f km", meters / 1000.0);
            } else {
                return meters + " m";
            }
        }
    }

    private void removeLocation(Location location) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Removal");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to remove " + location.getName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    locationRepository.delete(location.getId());
                    loadLocations();
                } catch (Exception e) {
                    showAlert("Error", "Could not remove location: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Subscribe
    public void onWeatherUpdate(WeatherDataUpdatedEvent event) {
        Platform.runLater(this::loadLocations);
    }
}