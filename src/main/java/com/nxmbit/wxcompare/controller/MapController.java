package com.nxmbit.wxcompare.controller;

import com.nxmbit.wxcompare.view.MapView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MapController implements Initializable {

    @FXML
    private BorderPane mapContainer;

    private MapView mapView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mapView = new MapView();
        mapContainer.setCenter(mapView);
    }

    public void focusOnLocation(double lat, double lng) {
        mapView.centerMap(lat, lng);
    }

    public void addWeatherMarker(double lat, double lng, String location, String weatherInfo) {
        mapView.addWeatherMarker(lat, lng, location, weatherInfo);
    }
}