package com.nxmbit.wxcompare.service;

import com.nxmbit.wxcompare.model.Location;
import com.nxmbit.wxcompare.model.User;
import com.nxmbit.wxcompare.model.Weather;
import com.nxmbit.wxcompare.repository.LocationRepository;
import com.nxmbit.wxcompare.repository.UserRepository;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Map;
import java.util.concurrent.*;

public class WeatherDataManager {
    private static WeatherDataManager instance;

    private final WeatherService weatherService;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    // cache for weather data
    private final Map<Long, Weather> weatherCache = new ConcurrentHashMap<>();

    private final BooleanProperty updatingProperty = new SimpleBooleanProperty(false);
    private final StringProperty statusMessageProperty = new SimpleStringProperty("");

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory());

    private static final ExecutorService virtualThreadExecutor =
            Executors.newVirtualThreadPerTaskExecutor();

    private ScheduledFuture<?> updateTask;

    private WeatherDataManager(WeatherService weatherService,
                               LocationRepository locationRepository,
                               UserRepository userRepository) {
        this.weatherService = weatherService;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    public static synchronized WeatherDataManager getInstance() {
        if (instance == null) {
            UserRepository userRepo = new UserRepository();
            instance = new WeatherDataManager(
                    new WeatherService(userRepo),
                    new LocationRepository(),
                    userRepo
            );
            instance.initializeUpdateSchedule();
        }
        return instance;
    }

    // Get weather data for a location (from cache or fetched if not available)
    public CompletableFuture<Weather> getWeatherForLocation(Location location) {
        Weather cachedWeather = weatherCache.get(location.getId());

        if (cachedWeather != null) {
            return CompletableFuture.completedFuture(cachedWeather);
        }

        // Not in cache, fetch it
        return CompletableFuture.supplyAsync(() -> {
            try {
                Weather weather = weatherService.getCurrentWeatherForLocation(location);
                weatherCache.put(location.getId(), weather);
                return weather;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, virtualThreadExecutor);
    }

    public CompletableFuture<Void> refreshAllWeatherData() {
        return updateAllWeatherData();
    }

    private void initializeUpdateSchedule() {
        updateAllWeatherData();

        User user = userRepository.findFirstUser().orElse(new User());
        int updateIntervalMinutes = 30; // Default to 30 min

        if (user.getWeatherUpdateInterval() > 0) {
            updateIntervalMinutes = user.getWeatherUpdateInterval();
        }

        if (updateTask != null) {
            updateTask.cancel(false);
        }

        updateTask = scheduler.scheduleAtFixedRate(
                this::updateAllWeatherDataBlocking,
                updateIntervalMinutes,
                updateIntervalMinutes,
                TimeUnit.MINUTES
        );
    }

    public void updateScheduleFromSettings() {
        initializeUpdateSchedule();
    }

    private void updateAllWeatherDataBlocking() {
        try {
            updateAllWeatherData().get(); // Block until complete
        } catch (Exception e) {
            System.err.println("Error updating weather data: " + e.getMessage());
        }
    }

    private CompletableFuture<Void> updateAllWeatherData() {
        if (updatingProperty.get()) {
            return CompletableFuture.completedFuture(null); // Already updating
        }

        Platform.runLater(() -> {
            updatingProperty.set(true);
            statusMessageProperty.set("Updating weather data...");
        });

        return CompletableFuture.supplyAsync(locationRepository::findAll)
                .thenCompose(locations -> {
                    if (locations.isEmpty()) {
                        return CompletableFuture.completedFuture(null);
                    }

                    CompletableFuture<?>[] futures = locations.stream()
                            .map(location -> CompletableFuture.supplyAsync(() -> {
                                try {
                                    Platform.runLater(() ->
                                            statusMessageProperty.set("Updating weather for " + location.getName() + "...")
                                    );
                                    Weather weather = weatherService.getCurrentWeatherForLocation(location);
                                    weatherCache.put(location.getId(), weather);
                                    return null;
                                } catch (Exception e) {
                                    throw new CompletionException(e);
                                }
                            }, Executors.newVirtualThreadPerTaskExecutor()))
                            .toArray(CompletableFuture[]::new);

                    return CompletableFuture.allOf(futures);
                })
                .whenComplete((result, error) -> {
                    Platform.runLater(() -> {
                        updatingProperty.set(false);
                        if (error != null) {
                            statusMessageProperty.set("Error updating weather data");
                        } else {
                            statusMessageProperty.set("Weather data updated");

                            // Clear status after a delay
                            CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS)
                                    .execute(() -> {
                                        Platform.runLater(() -> {
                                            if (statusMessageProperty.get().equals("Weather data updated")) {
                                                statusMessageProperty.set("");
                                            }
                                        });
                                    });
                        }
                    });
                });
    }

    // Properties for UI
    public BooleanProperty updatingProperty() {
        return updatingProperty;
    }

    public StringProperty statusMessageProperty() {
        return statusMessageProperty;
    }
}