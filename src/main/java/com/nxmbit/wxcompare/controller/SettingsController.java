package com.nxmbit.wxcompare.controller;

import com.nxmbit.wxcompare.enums.SystemOfMeasurement;
import com.nxmbit.wxcompare.enums.TemperatureUnit;
import com.nxmbit.wxcompare.model.User;
import com.nxmbit.wxcompare.repository.UserRepository;
import com.nxmbit.wxcompare.util.ApiConnectionDialogUtil;
import com.nxmbit.wxcompare.util.ControllerUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static com.nxmbit.wxcompare.util.ApiConnectionDialogUtil.testGoogleMapsApiConnection;
import static com.nxmbit.wxcompare.util.ApiConnectionDialogUtil.testWeatherApiConnection;

public class SettingsController implements Initializable {

    @FXML
    private TextField apiKeyField;

    @FXML
    private TextField googleMapsApiKeyField;

    @FXML
    private Button saveButton;

    @FXML
    private ComboBox<TemperatureUnit> temperatureUnitComboBox;

    @FXML
    private ComboBox<SystemOfMeasurement> systemOfMeasurementComboBox;

    private final UserRepository userRepository = new UserRepository();
    private User user;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        temperatureUnitComboBox.getItems().addAll(TemperatureUnit.values());
        systemOfMeasurementComboBox.getItems().addAll(SystemOfMeasurement.values());

        loadUserSettings();
    }

    private void loadUserSettings() {
        user = userRepository.findFirstUser().orElse(new User());

        apiKeyField.setText(user.getOpenWeatherMapApiKey());
        googleMapsApiKeyField.setText(user.getGoogleMapsApiKey());
        temperatureUnitComboBox.setValue(user.getTemperatureUnit());
        systemOfMeasurementComboBox.setValue(user.getSystemOfMeasurement());
    }

    @FXML
    private void saveSettings() {
        if (user == null) {
            user = new User();
        }

        String weatherApiKey = apiKeyField.getText();
        String mapsApiKey = googleMapsApiKeyField.getText();

        user.setOpenWeatherMapApiKey(weatherApiKey);
        user.setGoogleMapsApiKey(mapsApiKey);
        user.setTemperatureUnit(temperatureUnitComboBox.getValue());
        user.setSystemOfMeasurement(systemOfMeasurementComboBox.getValue());

        saveButton.setDisable(true);

        // First test the weather API
        testWeatherApiConnection(weatherApiKey)
                .thenCompose(weatherResult -> {
                    if (weatherResult.isSuccess()) {
                        // If weather API is successful, test Google Maps API
                        return testGoogleMapsApiConnection(mapsApiKey)
                                .thenApply(mapsResult -> new ApiConnectionDialogUtil.ApiTestResults(weatherResult, mapsResult));
                    } else {
                        // Weather API failed, skip Google Maps test
                        return CompletableFuture.completedFuture(
                                new ApiConnectionDialogUtil.ApiTestResults(weatherResult, null));
                    }
                })
                .thenAccept(this::handleApiTestResults)
                .exceptionally(ex -> {
                    handleTestError(ex);
                    return null;
                });
    }


    private void handleApiTestResults(ApiConnectionDialogUtil.ApiTestResults results) {
        Platform.runLater(() -> {
            saveButton.setDisable(false);

            if (results.weatherResult.isSuccess()) {
                if (results.mapsResult != null && results.mapsResult.isSuccess()) {
                    // Both APIs successful
                    userRepository.save(user);
                    showSuccessAlert();
                } else if (results.mapsResult != null) {
                    // Weather API good, Maps API bad
                    ApiConnectionDialogUtil.showConnectionErrorDialog(
                            this::saveSettings,
                            ControllerUtils::loadSettingsView,
                            results.mapsResult);
                } else {
                    // Weather API good, Maps API not tested (shouldn't happen)
                    userRepository.save(user);
                    showSuccessAlert();
                }
            } else {
                // Weather API failed
                ApiConnectionDialogUtil.showConnectionErrorDialog(
                        this::saveSettings,
                        ControllerUtils::loadSettingsView,
                        results.weatherResult);
            }
        });
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your settings have been saved successfully.");
        alert.showAndWait();
    }

    private void handleTestError(Throwable ex) {
        Platform.runLater(() -> {
            saveButton.setDisable(false);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText("Failed to test API connection");
            alert.setContentText("Error: " + ex.getMessage());
            alert.showAndWait();
        });
    }
}