package com.nxmbit.wxcompare;

import atlantafx.base.theme.PrimerDark;
import com.nxmbit.wxcompare.controller.MainController;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

        // Initialize database connection
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
            String apiKey = user.getOpenWeatherMapApiKey();

            // Skip the check if no API key is set yet
            if (apiKey == null || apiKey.trim().isEmpty()) {
                ControllerUtils.loadSettingsView();
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                ApiConnectionTestService connectionService = new ApiConnectionTestService();
                try {
                    return connectionService.testApiConnection(apiKey);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(result -> {
                if (!result.isSuccess()) {
                    Platform.runLater(() -> {
                        ApiConnectionDialogUtil.checkApiConnection(
                                this::checkApiConnectionOnStartup,
                                ControllerUtils::loadSettingsView,
                                result);
                    });
                }
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    ApiConnectionDialogUtil.checkApiConnection(
                            this::checkApiConnectionOnStartup,
                            ControllerUtils::loadSettingsView,
                            new ApiConnectionTestService.ConnectionResult(false, "Connection error: " + ex.getMessage()));
                });
                return null;
            });
        } else {
            // No user found, navigate to settings
            ControllerUtils.loadSettingsView();
        }
    }

    public static HostServices getAppHostServices() {
        return hostServices;
    }

    public static void main(String[] args) {
        launch();
    }
}