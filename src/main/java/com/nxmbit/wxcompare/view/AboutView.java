package com.nxmbit.wxcompare.view;

import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import java.util.ResourceBundle;

public class AboutView extends VBox {

    private HostServices hostServices;

    public AboutView(HostServices hostServices) {
        // Configure the main container
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(30));

        ResourceBundle resources = ResourceBundle.getBundle("com.nxmbit.wxcompare.application");

        // Title
        Label titleLabel = new Label("About WxCompare");
        titleLabel.setStyle("-fx-font-size: 24px;");

        // Content container
        VBox contentBox = new VBox();
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setSpacing(15);

        // Version info
        Label versionText = new Label("WxCompare v" + resources.getString("app.version"));

        // Description
        Label descriptionLabel = new Label("A weather comparison application");

        // Top separator
        Separator topSeparator = new Separator();
        topSeparator.setPrefWidth(300);

        // Description text
        String description = "This application allows you to compare weather data from different locations. " +
                "You can add locations, view current weather conditions, and compare forecasts.";
        Text descriptionText = new Text(description);
        descriptionText.setWrappingWidth(500);
        descriptionText.setTextAlignment(TextAlignment.CENTER);

        // Bottom separator
        Separator bottomSeparator = new Separator();
        bottomSeparator.setPrefWidth(300);

        Label creditsLabel = new Label("Created by Paweł Hołownia");

        // githyb link
        HBox githubBox = new HBox(10);
        githubBox.setAlignment(Pos.CENTER);
        Label sourceCodeLabel = new Label("View source code on GitHub: ");

        Hyperlink githubLink = new Hyperlink(resources.getString("app.source.url"));
        githubLink.setOnAction((event) -> {
            hostServices.showDocument(githubLink.getText());
        });

        githubBox.getChildren().addAll(sourceCodeLabel, githubLink);


        // Add all elements to the content box
        contentBox.getChildren().addAll(
                versionText,
                descriptionLabel,
                topSeparator,
                descriptionText,
                bottomSeparator,
                creditsLabel,
                githubBox
        );

        // Add everything to the main container
        getChildren().addAll(titleLabel, contentBox);
    }
}