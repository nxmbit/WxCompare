package com.nxmbit.wxcompare.controller;

import com.nxmbit.wxcompare.model.Location;
import com.nxmbit.wxcompare.model.User;
import com.nxmbit.wxcompare.model.Weather;
import com.nxmbit.wxcompare.repository.LocationRepository;
import com.nxmbit.wxcompare.repository.UserRepository;
import com.nxmbit.wxcompare.service.WeatherDataManager;
import com.nxmbit.wxcompare.service.WeatherService;
import com.nxmbit.wxcompare.util.ControllerUtils;
import com.nxmbit.wxcompare.util.WeatherIconUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class LocationsListController implements Initializable {
    @FXML
    private VBox locationsContainer;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Label noLocationsLabel;

    private final LocationRepository locationRepository = new LocationRepository();
    private final UserRepository userRepository = new UserRepository();
    private final WeatherService weatherService = new WeatherService(userRepository);
    private final WeatherDataManager weatherDataManager = WeatherDataManager.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadLocations();
    }

    @FXML
    private void openLocationSearch() {
        ControllerUtils.loadView("locations-search-view.fxml");
    }

    private void loadLocations() {
        locationsContainer.getChildren().clear();
        loadingIndicator.setVisible(true);
        noLocationsLabel.setVisible(false);

        CompletableFuture.supplyAsync(locationRepository::findAll)
                .thenAccept(locations -> {
                    Platform.runLater(() -> {
                        if (locations.isEmpty()) {
                            loadingIndicator.setVisible(false);
                            noLocationsLabel.setVisible(true);
                            return;
                        }

                        loadingIndicator.setVisible(false);
                        populateLocationsWithWeather(locations);
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

    @FXML
    private void refreshWeatherData() {
        weatherDataManager.refreshAllWeatherData()
                .thenRun(this::loadLocations);
    }

    private void populateLocationsWithWeather(List<Location> locations) {
        User user = userRepository.findFirstUser().orElse(new User());

        locations.forEach(location -> {
            VBox locationCard = createLocationCard(location);
            locationsContainer.getChildren().add(locationCard);

            weatherDataManager.getWeatherForLocation(location)
                    .thenAccept(weather -> Platform.runLater(() ->
                            updateLocationCardWithWeather(locationCard, weather, user)))
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            Label errorLabel = new Label("Could not load weather data: " +
                                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                            errorLabel.setStyle("-fx-text-fill: red;");
                            locationCard.getChildren().add(errorLabel);
                        });
                        return null;
                    });
        });
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

        Button removeButton = new Button("Remove");
        removeButton.setOnAction(event -> removeLocation(location));

        topRow.getChildren().addAll(nameLabel, spacer, removeButton);

        card.getChildren().addAll(topRow, weatherLoading);

        return card;
    }

    private void updateLocationCardWithWeather(VBox locationCard, Weather weather, User user) {
        // Remove loading indicator
        locationCard.getChildren().removeIf(node -> node instanceof ProgressIndicator);

        // Weather info container
        GridPane weatherGrid = new GridPane();
        weatherGrid.setHgap(10);
        weatherGrid.setVgap(5);
        weatherGrid.setPadding(new Insets(10, 0, 0, 0));

        // Icon and temperature
        HBox mainWeatherBox = new HBox(10);
        mainWeatherBox.setAlignment(Pos.CENTER_LEFT);

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
        mainWeatherBox.getChildren().addAll(weatherIcon, tempBox);

        // Additional details
        Label humidityLabel = new Label(String.format("Humidity: %d%%", weather.getHumidity()));
        Label pressureLabel = new Label(String.format("Pressure: %d hPa", weather.getPressure()));

        String windSpeedUnit = user.getSystemOfMeasurement() == null ? "m/s" :
                user.getSystemOfMeasurement().toString().equals("METRIC") ? "m/s" : "mph";

        Label windLabel = new Label(
                String.format("Wind: %.1f %s", weather.getWindSpeed(), windSpeedUnit));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, dd MMM");
        LocalDateTime dateTime = LocalDateTime.ofInstant(weather.getTimestamp(), ZoneId.systemDefault());
        Label updatedLabel = new Label("Updated: " + formatter.format(dateTime));
        updatedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        weatherGrid.add(mainWeatherBox, 0, 0, 2, 1);
        weatherGrid.add(humidityLabel, 0, 1);
        weatherGrid.add(pressureLabel, 1, 1);
        weatherGrid.add(windLabel, 0, 2);
        weatherGrid.add(updatedLabel, 0, 3, 2, 1);

        locationCard.getChildren().add(weatherGrid);
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
}