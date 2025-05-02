package com.nxmbit.wxcompare.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class ControllerUtils {
    // Singleton instance of the content area that will be set once by MainController
    private static StackPane contentArea;

    public static void setContentArea(StackPane area) {
        contentArea = area;
    }

    public static void loadView(String fxmlFile) {
        if (contentArea == null) {
            throw new IllegalStateException("Content area has not been set. Call setContentArea first.");
        }

        try {
            Parent view = FXMLLoader.load(ControllerUtils.class.getResource("/com/nxmbit/wxcompare/" + fxmlFile));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setContent(Node content) {
        if (contentArea == null) {
            throw new IllegalStateException("Content area has not been set. Call setContentArea first.");
        }

        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }
    
    // for popup
    public static void loadSettingsView() {
        loadView("settings-view.fxml");
    }
}