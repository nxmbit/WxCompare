package com.nxmbit.wxcompare.controller;

import com.google.common.eventbus.Subscribe;
import com.nxmbit.wxcompare.event.WeatherDataUpdatedEvent;
import com.nxmbit.wxcompare.model.Location;
import com.nxmbit.wxcompare.model.User;
import com.nxmbit.wxcompare.model.Weather;
import com.nxmbit.wxcompare.repository.LocationRepository;
import com.nxmbit.wxcompare.repository.UserRepository;
import com.nxmbit.wxcompare.service.EventBusService;
import com.nxmbit.wxcompare.service.WeatherDataManager;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MapController implements Initializable {

    @FXML
    private WebView webView;

    private WebEngine webEngine;

    private final LocationRepository locationRepository = new LocationRepository();
    private final UserRepository userRepository = new UserRepository();
    private final WeatherDataManager weatherDataManager = WeatherDataManager.getInstance();

    private final Map<Long, Weather> locationWeatherMap = new ConcurrentHashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        EventBusService.register(this);
        webEngine = webView.getEngine();

        // Load html
        webEngine.loadContent(createMapHtml());

        // Execute JS when page is loaded
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                // make the Java object available to JavaScript
                webEngine.executeScript(
                        "window.javaConnector = {" +
                                "  centerMap: function(lat, lng) { window.centerMapFromJava(lat, lng); }," +
                                "  setZoom: function(zoomLevel) { window.setZoomFromJava(zoomLevel); }," +
                                "  addWeatherMarker: function(lat, lng, title, info) { window.addWeatherMarkerFromJava(lat, lng, title, info); }" +
                                "};"
                );

                // Initialize map after page loaded
                webEngine.executeScript("initializeMap()");

                loadLocations();
            }
        });
    }

    private void loadLocations() {
        CompletableFuture.supplyAsync(locationRepository::findAll)
                .thenAccept(locations -> {
                    Platform.runLater(() -> {
                        if (locations.isEmpty()) {
                            return;
                        }

                        loadWeatherDataForLocations(locations);
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        showAlert("Error", "Could not load locations: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                    return null;
                });
    }

    private void loadWeatherDataForLocations(List<Location> locations) {
        locationWeatherMap.clear();

        webEngine.executeScript("clearAllMarkers()");

        // array of futures, one for each location
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Location location : locations) {
            CompletableFuture<Void> future = weatherDataManager.getWeatherForLocation(location)
                    .thenAccept(weather -> {
                        if (weather != null) {
                            locationWeatherMap.put(location.getId(), weather);
                        }
                    });

            futures.add(future);
        }

        // update UI when all futures are complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> Platform.runLater(this::displayWeatherMarkers));
    }

    private void displayWeatherMarkers() {
        try {
            User user = userRepository.findFirstUser().orElse(new User());

            // Get all locations
            List<Location> locations = locationRepository.findAll();

            boolean isFirstLocation = true;

            // Add markers for each location with weather data
            for (Location location : locations) {
                Weather weather = locationWeatherMap.get(location.getId());
                if (weather != null) {
                    String weatherInfo = formatWeatherInfo(weather, user);

                    addWeatherMarker(
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getName(),
                            weatherInfo
                    );

                    if (isFirstLocation) {
                        centerMap(location.getLatitude(), location.getLongitude());
                        setZoom(10);
                        isFirstLocation = false;
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error displaying weather data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String formatWeatherInfo(Weather weather, User user) {
        StringBuilder info = new StringBuilder();

        String unitSymbol = " °K";
        if (user.getTemperatureUnit() != null) {
            unitSymbol = switch (user.getTemperatureUnit()) {
                case FAHRENHEIT -> " °F";
                case KELVIN -> " °K";
                case CELSIUS -> " °C";
            };
        }

        info.append(String.format("Temperature: %.1f%s\n", weather.getTemperature(), unitSymbol));
        info.append(String.format("Condition: %s\n", weather.getDescription()));
        info.append(String.format("Humidity: %d%%\n", weather.getHumidity()));

        String windSpeedUnit = user.getSystemOfMeasurement() == null ? "m/s" :
                user.getSystemOfMeasurement().toString().equals("METRIC") ? "m/s" : "mph";

        info.append(String.format("Wind: %.1f %s %s\n",
                weather.getWindSpeed(),
                windSpeedUnit,
                getWindDirection(weather.getWindDeg())));

        // Feels like temperature
        info.append(String.format("Feels like: %.1f%s\n", weather.getFeelsLike(), unitSymbol));

        // Pressure
        info.append(String.format("Pressure: %d hPa\n", weather.getPressure()));

        // Visibility
        info.append(String.format("Visibility: %s\n",
                formatVisibility(weather.getVisibilityInMeters(), user)));

        // Last updated time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, dd MMM");
        LocalDateTime dateTime = LocalDateTime.ofInstant(weather.getTimestamp(), ZoneId.systemDefault());
        info.append(String.format("Updated: %s", formatter.format(dateTime)));

        return info.toString();
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

    public void centerMap(double lat, double lng) {
        webEngine.executeScript(String.format("setMapCenter(%f, %f)", lat, lng));
    }

    public void setZoom(int zoomLevel) {
        webEngine.executeScript(String.format("setMapZoom(%d)", zoomLevel));
    }

    public void addWeatherMarker(double lat, double lng, String title, String weatherInfo) {
        String script = String.format(
                "addWeatherMarker(%f, %f, '%s', '%s')",
                lat, lng, escapeJavaScript(title), escapeJavaScript(weatherInfo)
        );
        webEngine.executeScript(script);
    }

    public void refreshWeatherData() {
        loadLocations();
    }

    // Helper method to escape single quotes for JavaScript
    private String escapeJavaScript(String input) {
        if (input == null) return "";
        return input.replace("'", "\\'").replace("\n", "\\n");
    }

    // Create HTML content with Leaflet
    private String createMapHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.3/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.3/dist/leaflet.js"></script>
                <style>
                    html, body, #map {
                        height: 100%;
                        width: 100%;
                        margin: 0;
                        padding: 0;
                    }

                    .temp-marker {
                        background-color: rgba(0, 120, 255, 0.8);
                        color: white;
                        border-radius: 50%;
                        width: 40px;
                        height: 40px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-weight: bold;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.3);
                        font-size: 14px;
                    }

                    .weather-popup h3 {
                        margin-top: 0;
                        margin-bottom: 5px;
                    }

                    .weather-details {
                        white-space: pre-line;
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    let map;
                    let markers = {};

                    function initializeMap() {
                        map = L.map('map').setView([40.748817, -73.985428], 10);

                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                        }).addTo(map);
                    }

                    function setMapCenter(lat, lng) {
                        if (map) {
                            map.setView([lat, lng]);
                        }
                    }

                    function setMapZoom(zoomLevel) {
                        if (map) {
                            map.setZoom(zoomLevel);
                        }
                    }

                    function addWeatherMarker(lat, lng, title, weatherInfo) {
                        if (map) {
                            const markerId = `${lat},${lng}`;

                            // Remove existing marker at this location
                            if (markers[markerId]) {
                                map.removeLayer(markers[markerId]);
                            }

                            // Extract temperature from weatherInfo to display on marker
                            const tempMatch = weatherInfo.match(/(\\d+(\\.\\d+)?)\\s*°/);
                            const temp = tempMatch ? tempMatch[1] : "?";

                            // Create a custom icon with temperature display
                            const weatherIcon = L.divIcon({
                                className: 'weather-marker-icon',
                                html: `<div class="temp-marker">${temp}°</div>`,
                                iconSize: [40, 40],
                                iconAnchor: [20, 40]
                            });

                            // Create the marker with the custom icon
                            const marker = L.marker([lat, lng], {
                                icon: weatherIcon,
                                title: title
                            }).addTo(map);

                            // Add popup with detailed weather info
                            marker.bindPopup(`
                                <div class="weather-popup">
                                    <h3>${title}</h3>
                                    <div class="weather-details">
                                        ${weatherInfo}
                                    </div>
                                </div>
                            `);

                            markers[markerId] = marker;
                        }
                    }
                    
                    function clearAllMarkers() {
                        if (map) {
                            Object.values(markers).forEach(marker => {
                                map.removeLayer(marker);
                            });
                            markers = {};
                        }
                    }
                    
                    function openMarkerPopup(lat, lng) {
                        const markerId = `${lat},${lng}`;
                        if (markers[markerId]) {
                            markers[markerId].openPopup();
                        }
                    }

                    // bridge functions between java and js
                    window.centerMapFromJava = function(lat, lng) {
                        setMapCenter(lat, lng);
                    };

                    window.setZoomFromJava = function(zoomLevel) {
                        setMapZoom(zoomLevel);
                    };

                    window.addWeatherMarkerFromJava = function(lat, lng, title, info) {
                        addWeatherMarker(lat, lng, title, info);
                    };
                </script>
            </body>
            </html>
            """;
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