package com.nxmbit.wxcompare.model;

import com.nxmbit.wxcompare.enums.SystemOfMeasurement;
import com.nxmbit.wxcompare.enums.TemperatureUnit;
import jakarta.persistence.*;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String openWeatherMapApiKey;

    private String googleMapsApiKey;

    @Enumerated(EnumType.STRING)
    private TemperatureUnit temperatureUnit = TemperatureUnit.CELSIUS;

    @Enumerated(EnumType.STRING)
    private SystemOfMeasurement systemOfMeasurement = SystemOfMeasurement.METRIC;

    private int weatherUpdateInterval = 10; // minutes

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpenWeatherMapApiKey() {
        return openWeatherMapApiKey;
    }

    public void setOpenWeatherMapApiKey(String openWeatherMapApiKey) {
        this.openWeatherMapApiKey = openWeatherMapApiKey;
    }

    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(TemperatureUnit temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public SystemOfMeasurement getSystemOfMeasurement() {
        return systemOfMeasurement;
    }

    public void setSystemOfMeasurement(SystemOfMeasurement systemOfMeasurement) {
        this.systemOfMeasurement = systemOfMeasurement;
    }

    public String getGoogleMapsApiKey() {
        return googleMapsApiKey;
    }

    public void setGoogleMapsApiKey(String googleMapsApiKey) {
        this.googleMapsApiKey = googleMapsApiKey;
    }

    public int getWeatherUpdateInterval() {
        return weatherUpdateInterval;
    }

    public void setWeatherUpdateInterval(int weatherUpdateInterval) {
        this.weatherUpdateInterval = weatherUpdateInterval;
    }
}
