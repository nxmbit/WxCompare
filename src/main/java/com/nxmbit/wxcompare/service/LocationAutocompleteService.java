package com.nxmbit.wxcompare.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxmbit.wxcompare.model.Location;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class LocationAutocompleteService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public LocationAutocompleteService(String apiKey) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
    }

    public ObservableList<Location> searchLocations(String query) throws IOException, InterruptedException {
        if (query == null || query.trim().isEmpty() || query.length() < 2) {
            return FXCollections.observableArrayList();
        }

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json"
                + "?input=" + encodedQuery
                + "&types=(cities)"
                + "&key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error searching locations: " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());

        if (!"OK".equals(root.path("status").asText()) && !"ZERO_RESULTS".equals(root.path("status").asText())) {
            throw new IOException("API error: " + root.path("status").asText());
        }

        List<Location> locations = new ArrayList<>();
        JsonNode predictions = root.path("predictions");

        for (JsonNode prediction : predictions) {
            // Need to make additional request to get coordinates
            String placeId = prediction.path("place_id").asText();
            Location location = getLocationDetails(placeId);
            if (location != null) {
                locations.add(location);
            }
        }

        return FXCollections.observableArrayList(locations);
    }

    private Location getLocationDetails(String placeId) throws IOException, InterruptedException {
        String url = "https://maps.googleapis.com/maps/api/place/details/json"
                + "?place_id=" + placeId
                + "&fields=name,geometry"
                + "&key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        JsonNode root = objectMapper.readTree(response.body());

        if (!"OK".equals(root.path("status").asText())) {
            return null;
        }

        JsonNode result = root.path("result");
        Location location = new Location();
        location.setName(result.path("name").asText());
        location.setLatitude(result.path("geometry").path("location").path("lat").asDouble());
        location.setLongitude(result.path("geometry").path("location").path("lng").asDouble());

        return location;
    }
}