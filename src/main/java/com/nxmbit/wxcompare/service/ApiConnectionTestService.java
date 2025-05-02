package com.nxmbit.wxcompare.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class ApiConnectionTestService {

    private static final String WEATHER_API_TEST_URL = "https://api.openweathermap.org/data/2.5/weather?q=London&appid=%s";
    private static final String GMAPS_API_TEST_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=London&key=%s";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ConnectionResult testApiConnection(String apiKey) throws InterruptedException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return new ConnectionResult(false, "API key is missing.");
        }
        Thread.sleep(1000);
        return testConnection(String.format(WEATHER_API_TEST_URL, apiKey), false);
    }

    public ConnectionResult testGoogleMapsApiConnection(String apiKey) throws InterruptedException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return new ConnectionResult(false, "Google Maps API key is missing.");
        }
        Thread.sleep(1000);
        return testConnection(String.format(GMAPS_API_TEST_URL, apiKey), true);
    }

    private ConnectionResult testConnection(String urlString, boolean isGoogleApi) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                if (!isGoogleApi && responseCode == 401) {
                    return new ConnectionResult(false, "Invalid OpenWeatherMap API key");
                } else {
                    return new ConnectionResult(false, "OpenWeatherMap API error: " + responseCode);
                }
            }

            if (isGoogleApi) {
                String response = new BufferedReader(new InputStreamReader(connection.getInputStream()))
                        .lines().collect(Collectors.joining("\n"));

                JsonNode jsonResponse = objectMapper.readTree(response);
                String status = jsonResponse.get("status").asText();

                if ("OK".equals(status)) {
                    return new ConnectionResult(true, "Connection successful");
                } else if ("REQUEST_DENIED".equals(status) && jsonResponse.has("error_message")) {
                    String errorMessage = "Google Maps API error: " + jsonResponse.get("error_message").asText();
                    return new ConnectionResult(false, errorMessage);
                } else {
                    return new ConnectionResult(false, "Google Maps API error: " + status);
                }
            }

            return new ConnectionResult(true, "Connection successful");

        } catch (IOException e) {
            if (e.getMessage().contains("UnknownHostException") ||
                    e.getMessage().contains("ConnectException") ||
                    e.getMessage().contains("SocketTimeoutException")) {
                return new ConnectionResult(false, "No internet connection. Please check your network settings.");
            } else {
                return new ConnectionResult(false, "Connection error: " + e.getMessage());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static class ConnectionResult {
        private final boolean success;
        private final String message;

        public ConnectionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}