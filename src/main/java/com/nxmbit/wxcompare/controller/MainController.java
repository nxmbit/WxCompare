package com.nxmbit.wxcompare.controller;

import com.google.common.eventbus.Subscribe;
import com.nxmbit.wxcompare.WxCompareApplication;
import com.nxmbit.wxcompare.service.EventBusService;
import com.nxmbit.wxcompare.event.KeyboardShortcutEvent;
import com.nxmbit.wxcompare.util.ControllerUtils;
import com.nxmbit.wxcompare.view.AboutView;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import static com.nxmbit.wxcompare.util.ControllerUtils.loadView;
import static com.nxmbit.wxcompare.util.ControllerUtils.setContent;

public class MainController {
    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        EventBusService.register(this);

        ControllerUtils.setContentArea(contentArea);

        loadView("locations-list-view.fxml");
    }

    @Subscribe
    public void handleKeyboardShortcut(KeyboardShortcutEvent event) {
        switch (event.getAction()) {
            case KeyboardShortcutEvent.NAVIGATE_LOCATIONS_LIST:
                loadView("locations-list-view.fxml");
                break;
            case KeyboardShortcutEvent.NAVIGATE_LOCATIONS_SEARCH:
                loadView("locations-search-view.fxml");
                break;
            case KeyboardShortcutEvent.NAVIGATE_KEYBOARD_SHORTCUTS:
                loadView("keyboard-shortcuts-view.fxml");
                break;
            case KeyboardShortcutEvent.OPEN_SETTINGS:
                loadView("settings-view.fxml");
                break;
            case KeyboardShortcutEvent.NAVIGATE_MAP:
                loadView("map-view.fxml");
                break;
            case KeyboardShortcutEvent.NAVIGATE_ABOUT:
                AboutView aboutView = new AboutView(WxCompareApplication.getAppHostServices());
                setContent(aboutView);
                break;
        }
    }
}