package com.nxmbit.wxcompare.controller;

import com.nxmbit.wxcompare.model.SystemOfMeasurement;
import com.nxmbit.wxcompare.model.TemperatureUnit;
import com.nxmbit.wxcompare.model.User;
import com.nxmbit.wxcompare.repository.UserRepository;
import com.nxmbit.wxcompare.service.ApiConnectionTestService;
import com.nxmbit.wxcompare.util.ApiConnectionDialogUtil;
import com.nxmbit.wxcompare.util.ControllerUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class SettingsController implements Initializable {

    @FXML
    private TextField apiKeyField;

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

        if (user != null) {
            apiKeyField.setText(user.getOpenWeatherMapApiKey());
            temperatureUnitComboBox.setValue(user.getTemperatureUnit());
            systemOfMeasurementComboBox.setValue(user.getSystemOfMeasurement());
        }
    }

    @FXML
    private void saveSettings() {
        if (user == null) {
            user = new User();
        }

        String apiKey = apiKeyField.getText();
        user.setOpenWeatherMapApiKey(apiKey);
        user.setTemperatureUnit(temperatureUnitComboBox.getValue());
        user.setSystemOfMeasurement(systemOfMeasurementComboBox.getValue());

        saveButton.setDisable(true);

        CompletableFuture.supplyAsync(() -> {
            ApiConnectionTestService connectionService = new ApiConnectionTestService();
            try {
                return connectionService.testApiConnection(apiKey);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(result -> {
            Platform.runLater(() -> {
                saveButton.setDisable(false);

                if (result.isSuccess()) {
                    userRepository.save(user);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Settings Saved");
                    alert.setHeaderText(null);
                    alert.setContentText("Your settings have been saved successfully.");
                    alert.showAndWait();
                } else {
                    ApiConnectionDialogUtil.checkApiConnection(this::saveSettings,
                            ControllerUtils::loadSettingsView,
                            result);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                saveButton.setDisable(false);

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Connection Error");
                alert.setHeaderText("Failed to test API connection");
                alert.setContentText("Error: " + ex.getMessage());
                alert.showAndWait();
            });
            return null;
        });
    }
}