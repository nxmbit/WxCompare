package com.nxmbit.wxcompare.controller;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.google.common.eventbus.Subscribe;
import com.nxmbit.wxcompare.WxCompareApplication;
import com.nxmbit.wxcompare.view.AboutView;
import com.nxmbit.wxcompare.service.EventBusService;
import com.nxmbit.wxcompare.event.KeyboardShortcutEvent;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ResourceBundle;

import static com.nxmbit.wxcompare.util.ControllerUtils.loadView;
import static com.nxmbit.wxcompare.util.ControllerUtils.setContent;

public class SidebarController {

    @FXML
    private FontIcon themeIcon;

    @FXML
    private Label versionLabel;

    private MainController mainController;
    private ResourceBundle resources;
    private boolean isDarkTheme = true;

    @FXML
    public void initialize() {
        resources = ResourceBundle.getBundle("com.nxmbit.wxcompare.application");
        versionLabel.setText("v" + resources.getString("app.version"));
        EventBusService.register(this);
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    private void setLightTheme() {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    }

    private void setDarkTheme() {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
    }

    @FXML
    private void navigateToDashboard(ActionEvent event) {
        loadView("dashboard-view.fxml");
    }

    @FXML
    private void navigateToLocations(ActionEvent event) {
        loadView("locations-view.fxml");
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