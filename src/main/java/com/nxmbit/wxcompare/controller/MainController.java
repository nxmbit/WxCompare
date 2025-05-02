package com.nxmbit.wxcompare.controller;

import com.google.common.eventbus.Subscribe;
import com.nxmbit.wxcompare.service.EventBusService;
import com.nxmbit.wxcompare.event.KeyboardShortcutEvent;
import com.nxmbit.wxcompare.util.ControllerUtils;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import static com.nxmbit.wxcompare.util.ControllerUtils.loadView;

public class MainController {
    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        EventBusService.register(this);

        ControllerUtils.setContentArea(contentArea);

        // Load default view
        loadView("dashboard-view.fxml");
    }

    @Subscribe
    public void handleKeyboardShortcut(KeyboardShortcutEvent event) {
        switch (event.getAction()) {
            case KeyboardShortcutEvent.NAVIGATE_DASHBOARD:
                loadView("dashboard-view.fxml");
                break;
            case KeyboardShortcutEvent.NAVIGATE_LOCATIONS:
                loadView("locations-view.fxml");
                break;
            case KeyboardShortcutEvent.NAVIGATE_COMPARISON:
                loadView("comparison-view.fxml");
                break;
            case KeyboardShortcutEvent.OPEN_SETTINGS:
                loadView("settings-view.fxml");
                break;
            case KeyboardShortcutEvent.NAVIGATE_MAP:
                loadView("map-view.fxml");
                break;
        }
    }
}