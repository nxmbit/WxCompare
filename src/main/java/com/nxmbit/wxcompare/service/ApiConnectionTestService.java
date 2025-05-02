package com.nxmbit.wxcompare.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiConnectionTestService {

    private static final String API_TEST_URL = "https://api.openweathermap.org/data/2.5/weather?q=London&appid=%s";

    /**
     * Tests the OpenWeatherMap API connection with the given API key
     * @param apiKey The API key to test
     * @return A ConnectionResult object containing status and message
     */
    public ConnectionResult testApiConnection(String apiKey) throws InterruptedException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return new ConnectionResult(false, "API key is missing.");
        }

        Thread.sleep(4000); // Simulate a delay for testing purposes

        HttpURLConnection connection = null;
        try {
            URL url = new URL(String.format(API_TEST_URL, apiKey));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                return new ConnectionResult(true, "Connection successful");
            } else if (responseCode == 401) {
                return new ConnectionResult(false, "Invalid API key");
            } else {
                return new ConnectionResult(false, "API error: " + responseCode);
            }

        } catch (IOException e) {
            // Check if the error is related to network connectivity
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

    // Simple result class to hold connection test results
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