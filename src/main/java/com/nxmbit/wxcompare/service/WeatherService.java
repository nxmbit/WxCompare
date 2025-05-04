package com.nxmbit.wxcompare.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxmbit.wxcompare.model.Location;
import com.nxmbit.wxcompare.model.User;
import com.nxmbit.wxcompare.model.Weather;
import com.nxmbit.wxcompare.repository.UserRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

public class WeatherService {
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=%s";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public WeatherService(UserRepository userRepository) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.userRepository = userRepository;
    }

    public Weather getCurrentWeatherForLocation(Location location) throws IOException, InterruptedException {
        User user = userRepository.findFirstUser().orElseThrow(() -> new IllegalStateException("No user found"));
        String apiKey = user.getOpenWeatherMapApiKey();

        String units = "standard";

        if (user.getTemperatureUnit() != null) {
            units = switch (user.getTemperatureUnit()) {
                case CELSIUS -> "metric";
                case FAHRENHEIT -> "imperial";
                case KELVIN -> "standard";
            };
        }

        String url = String.format(
                WEATHER_API_URL,
                location.getLatitude(),
                location.getLongitude(),
                apiKey,
                units
        );

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Weather API error: " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        Weather weather = new Weather();

        // Parse main weather info
        if (root.has("weather") && root.get("weather").isArray() && !root.get("weather").isEmpty()) {
            JsonNode weatherNode = root.get("weather").get(0);
            weather.setMain(weatherNode.get("main").asText());
            weather.setDescription(weatherNode.get("description").asText());
            weather.setIconCode(weatherNode.get("icon").asText());
        }

        // Parse temperature and other measurements
        if (root.has("main")) {
            JsonNode mainNode = root.get("main");
            weather.setTemperature(mainNode.get("temp").asDouble());
            weather.setFeelsLike(mainNode.get("feels_like").asDouble());
            weather.setTempMin(mainNode.get("temp_min").asDouble());
            weather.setTempMax(mainNode.get("temp_max").asDouble());
            weather.setPressure(mainNode.get("pressure").asInt());
            weather.setHumidity(mainNode.get("humidity").asInt());
        }

        // Parse wind data
        if (root.has("wind")) {
            JsonNode windNode = root.get("wind");
            weather.setWindSpeed(windNode.get("speed").asDouble());
            weather.setWindDeg(windNode.get("deg").asInt());
        }

        // Parse clouds data
        if (root.has("clouds")) {
            JsonNode cloudsNode = root.get("clouds");
            weather.setCloudinessPercent(cloudsNode.get("all").asInt());
        }

        // Parse rain data
        if (root.has("rain") && root.get("rain").has("1h")) {
            weather.setRainMmLastHour(root.get("rain").get("1h").asInt());
        }

        // Parse snow data
        if (root.has("snow") && root.get("snow").has("1h")) {
            weather.setSnowMmLastHour(root.get("snow").get("1h").asInt());
        }

        // update timestamp
        weather.setTimestamp(Instant.now());

        if (root.has("sys")) {
            JsonNode sysNode = root.get("sys");
            weather.setSunrise(Instant.ofEpochSecond(sysNode.get("sunrise").asLong()));
            weather.setSunset(Instant.ofEpochSecond(sysNode.get("sunset").asLong()));
        }

        // Parse visibility
        if (root.has("visibility")) {
            weather.setVisibilityInMeters(root.get("visibility").asInt());
        }

        return weather;
    }
}