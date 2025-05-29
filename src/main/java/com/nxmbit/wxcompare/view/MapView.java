package com.nxmbit.wxcompare.view;

import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class MapView extends BorderPane {

    private WebView webView;
    private WebEngine webEngine;

    public MapView() {
        webView = new WebView();
        webEngine = webView.getEngine();

        setPadding(new Insets(10));
        setCenter(webView);

        // Load HTML with Leaflet map
        webEngine.loadContent(createMapHtml());

        // Execute JS when page is loaded
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                // Make the Java object available to JavaScript using the new approach
                webEngine.executeScript(
                        "window.javaConnector = {" +
                                "  centerMap: function(lat, lng) { window.centerMapFromJava(lat, lng); }," +
                                "  setZoom: function(zoomLevel) { window.setZoomFromJava(zoomLevel); }," +
                                "  addWeatherMarker: function(lat, lng, title, info) { window.addWeatherMarkerFromJava(lat, lng, title, info); }" +
                                "};"
                );

                // Initialize map after page loaded
                webEngine.executeScript("initializeMap()");
            }
        });
    }

    // Methods that can be called from Java to manipulate the map
    public void centerMap(double lat, double lng) {
        webEngine.executeScript(String.format("setMapCenter(%f, %f)", lat, lng));
    }

    public void setZoom(int zoomLevel) {
        webEngine.executeScript(String.format("setMapZoom(%d)", zoomLevel));
    }

    // Method that will eventually allow adding weather markers
    public void addWeatherMarker(double lat, double lng, String title, String weatherInfo) {
        String script = String.format(
                "addWeatherMarker(%f, %f, '%s', '%s')",
                lat, lng, escapeJavaScript(title), escapeJavaScript(weatherInfo)
        );
        webEngine.executeScript(script);
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
                            const tempMatch = weatherInfo.match(/(\\d+(\\.\\d+)?)\\s*°C/);
                            const temp = tempMatch ? tempMatch[1] : "?";
                           \s
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
                           \s
                            // Add popup with detailed weather info
                            marker.bindPopup(`
                                <div class="weather-popup">
                                    <h3>${title}</h3>
                                    <div class="weather-details">
                                        ${weatherInfo}
                                    </div>
                                </div>
                            `);
                           \s
                            markers[markerId] = marker;
                        }
                       }

                    // Bridge functions between Java and JavaScript
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
}