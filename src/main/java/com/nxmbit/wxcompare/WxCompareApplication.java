package com.nxmbit.wxcompare;

import atlantafx.base.theme.PrimerDark;
import com.nxmbit.wxcompare.model.User;
import com.nxmbit.wxcompare.repository.UserRepository;
import com.nxmbit.wxcompare.service.ApiConnectionTestService;
import com.nxmbit.wxcompare.util.ApiConnectionDialogUtil;
import com.nxmbit.wxcompare.util.ControllerUtils;
import com.nxmbit.wxcompare.util.KeyboardShortcutRegistrar;
import com.nxmbit.wxcompare.util.SqliteDbUtil;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.nxmbit.wxcompare.util.ApiConnectionDialogUtil.testGoogleMapsApiConnection;
import static com.nxmbit.wxcompare.util.ApiConnectionDialogUtil.testWeatherApiConnection;

public class WxCompareApplication extends Application {

    private static HostServices hostServices;
    private final UserRepository userRepository = new UserRepository();

    @Override
    public void start(Stage stage) throws IOException {
        hostServices = getHostServices();

        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        FXMLLoader fxmlLoader = new FXMLLoader(WxCompareApplication.class.getResource("main-layout.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);

        KeyboardShortcutRegistrar.registerShortcuts(scene);

        scene.getStylesheets().add(getClass().getResource("/com/nxmbit/wxcompare/styles/styles.css").toExternalForm());

        Image appIcon = new Image(WxCompareApplication.class.getResourceAsStream("/com/nxmbit/wxcompare/assets/logo.png"));
        stage.getIcons().add(appIcon);

        SqliteDbUtil.getSessionFactory();

        stage.setTitle("WxCompare");
        stage.setScene(scene);
        stage.show();

        checkApiConnectionOnStartup();
    }

    private void checkApiConnectionOnStartup() {
        Optional<User> userOpt = userRepository.findFirstUser();

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String weatherApiKey = user.getOpenWeatherMapApiKey();
            String mapsApiKey = user.getGoogleMapsApiKey();

            // Skip the check if some keys are empty
            if ((weatherApiKey == null || weatherApiKey.trim().isEmpty()) ||
                    (mapsApiKey == null || mapsApiKey.trim().isEmpty())) {
                ControllerUtils.loadSettingsView();
                return;
            }

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
                    .thenAccept(results -> handleApiTestResults(results))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            ApiConnectionDialogUtil.showConnectionErrorDialog(
                                    this::checkApiConnectionOnStartup,
                                    ControllerUtils::loadSettingsView,
                                    new ApiConnectionTestService.ConnectionResult(false, "Connection error: " + ex.getMessage()));
                        });
                        return null;
                    });
        } else {
            // No user found, go to settings view
            ControllerUtils.loadSettingsView();
        }
    }

    private void handleApiTestResults(ApiConnectionDialogUtil.ApiTestResults results) {
        Platform.runLater(() -> {
            if (results.weatherResult != null && !results.weatherResult.isSuccess()) {
                // Weather API failed
                ApiConnectionDialogUtil.showConnectionErrorDialog(
                        this::checkApiConnectionOnStartup,
                        ControllerUtils::loadSettingsView,
                        results.weatherResult);
            } else if (results.mapsResult != null && !results.mapsResult.isSuccess()) {
                // Weather API is fine, but Maps API failed
                ApiConnectionDialogUtil.showConnectionErrorDialog(
                        this::checkApiConnectionOnStartup,
                        ControllerUtils::loadSettingsView,
                        results.mapsResult);
            }
        });
    }

    public static HostServices getAppHostServices() {
        return hostServices;
    }

    public static void main(String[] args) {
        launch();
    }
}