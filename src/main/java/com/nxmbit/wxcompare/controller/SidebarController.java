package com.nxmbit.wxcompare.controller;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import com.google.common.eventbus.Subscribe;
import com.nxmbit.wxcompare.WxCompareApplication;
import com.nxmbit.wxcompare.service.WeatherDataManager;
import com.nxmbit.wxcompare.view.AboutView;
import com.nxmbit.wxcompare.service.EventBusService;
import com.nxmbit.wxcompare.event.KeyboardShortcutEvent;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ResourceBundle;

import static com.nxmbit.wxcompare.util.ControllerUtils.loadView;
import static com.nxmbit.wxcompare.util.ControllerUtils.setContent;

public class SidebarController {

    @FXML
    private FontIcon themeIcon;

    @FXML
    private Label versionLabel;

    @FXML
    private ProgressIndicator statusIndicator;

    @FXML
    private Label statusLabel;

    @FXML
    private HBox statusContainer;

    private ResourceBundle resources;
    private boolean isDarkTheme = true;
    private final WeatherDataManager weatherDataManager = WeatherDataManager.getInstance();

    @FXML
    public void initialize() {
        resources = ResourceBundle.getBundle("com.nxmbit.wxcompare.application");
        versionLabel.setText("v" + resources.getString("app.version"));
        EventBusService.register(this);

        // Bind the status indicator to the weather data manager
        statusIndicator.visibleProperty().bind(weatherDataManager.updatingProperty());
        statusLabel.textProperty().bind(weatherDataManager.statusMessageProperty());

        // Make the entire container invisible when there's no status message
        statusContainer.visibleProperty().bind(
                weatherDataManager.updatingProperty().or(
                        weatherDataManager.statusMessageProperty().isNotEmpty()
                )
        );
    }

    private void setLightTheme() {
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
    }

    private void setDarkTheme() {
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
    }

    @FXML
    private void navigateToDashboard(ActionEvent event) {
        loadView("dashboard-view.fxml");
    }

    @FXML
    private void navigateToLocationsSearch(ActionEvent event) {
        loadView("locations-search-view.fxml");
    }

    @FXML
    private void navigateToLocationsList(ActionEvent event) {
        loadView("locations-list-view.fxml");
    }

    @FXML
    private void navigateToComparison(ActionEvent event) {
        loadView("comparison-view.fxml");
    }

    @FXML
    private void navigateToSettings(ActionEvent event) {
        loadView("settings-view.fxml");
    }

    @FXML
    private void navigateToMap(ActionEvent event) {
        loadView("map-view.fxml");
    }

    @FXML
    private void navigateToAbout(ActionEvent event) {
        AboutView aboutView = new AboutView(WxCompareApplication.getAppHostServices());
        setContent(aboutView);
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        toggleThemeImpl();
    }

    private void toggleThemeImpl() {
        if (isDarkTheme) {
            setLightTheme();
            themeIcon.setIconLiteral("fth-sun");
        } else {
            setDarkTheme();
            themeIcon.setIconLiteral("fth-moon");
        }
        isDarkTheme = !isDarkTheme;
    }

    @Subscribe
    public void handleKeyboardShortcut(KeyboardShortcutEvent event) {
        if (event.getAction().equals(KeyboardShortcutEvent.TOGGLE_THEME)) {
            toggleThemeImpl();
        }
    }
}