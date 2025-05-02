package com.nxmbit.wxcompare.util;

import com.nxmbit.wxcompare.service.ApiConnectionTestService;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class ApiConnectionDialogUtil {

    public static void checkApiConnection(Runnable retryCallback, Runnable settingsCallback,
                                          ApiConnectionTestService.ConnectionResult result) {
        // Connection failed, show dialog
        boolean isNetworkError = result.getMessage().contains("internet") ||
                result.getMessage().contains("network");

        ButtonType retryButton = new ButtonType("Retry", ButtonBar.ButtonData.OK_DONE);
        ButtonType settingsButton = new ButtonType("Settings", ButtonBar.ButtonData.OTHER);
        ButtonType exitButton = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.ERROR,
                result.getMessage(),
                retryButton,
                isNetworkError ? null : settingsButton,
                exitButton);

        alert.setTitle("API Connection Error");
        alert.setHeaderText(isNetworkError ? "Network Connection Error" : "API Key Error");

        Optional<ButtonType> response = alert.showAndWait();

        if (response.isPresent()) {
            ButtonType button = response.get();

            if (button == retryButton) {
                if (retryCallback != null) {
                    retryCallback.run();
                }
            } else if (button == settingsButton) {
                if (settingsCallback != null) {
                    settingsCallback.run();
                }
            } else {
                Platform.exit();
            }
        }
    }
}