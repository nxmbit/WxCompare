package com.nxmbit.wxcompare.controller;

import com.nxmbit.wxcompare.model.Location;
import com.nxmbit.wxcompare.model.User;
import com.nxmbit.wxcompare.repository.UserRepository;
import com.nxmbit.wxcompare.service.LocationAutocompleteService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class LocationsSearchController {
    @FXML
    private TextField searchField;

    @FXML
    private ListView<Location> resultsListView;

    @FXML
    private Label statusLabel;

    @FXML
    private Label errorLabel;

    private LocationAutocompleteService autocompleteService;

    private final UserRepository userRepository = new UserRepository();

    @FXML
    public void initialize() {
        String apiKey = userRepository.findFirstUser()
                .map(User::getGoogleMapsApiKey)
                .orElse("");

        autocompleteService = new LocationAutocompleteService(apiKey);

        // Configure cell factory to display location names
        resultsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Location location, boolean empty) {
                super.updateItem(location, empty);
                setText(empty || location == null ? "" : location.getName());
            }
        });

        // Add listener to search field with debounce to avoid too many API calls
        PauseTransition pause = new PauseTransition(Duration.millis(250));
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            statusLabel.setText("Searching...");
            pause.setOnFinished(e -> performSearch(newValue));
            pause.playFromStart();
        });

        // Handle selection event
        resultsListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        handleLocationSelected(newValue);
                    }
                });
    }

    private void performSearch(String query) {
        errorLabel.setVisible(false);

        if (query == null || query.trim().length() < 2) {
            resultsListView.setItems(FXCollections.observableArrayList());
            statusLabel.setText("Type at least 2 characters to search");
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                return autocompleteService.searchLocations(query);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(locations -> {
            Platform.runLater(() -> {
                resultsListView.setItems(locations);

                if (locations.isEmpty()) {
                    statusLabel.setText("No results found");
                } else {
                    statusLabel.setText(locations.size() + " locations found");
                }
            });
        }).exceptionally(e -> {
            // Handle exceptions on the JavaFX Application Thread
            Platform.runLater(() -> {
                resultsListView.setItems(FXCollections.observableArrayList());
                errorLabel.setText("Error: " + e.getCause().getMessage());
                errorLabel.setVisible(true);
                statusLabel.setText("Search failed");
            });
            return null;
        });
    }

    private void handleLocationSelected(Location location) {
        // Here you can:
        // 1. Save to database
        // 2. Navigate to location details
        // 3. Fetch weather for this location
        // 4. etc.

        statusLabel.setText("Selected: " + location.getName());

        // Example: you might want to save this location to the user's favorites
        // locationService.saveUserLocation(location);
    }
}