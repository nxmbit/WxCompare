package com.nxmbit.wxcompare.model;

import jakarta.persistence.*;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String openWeatherMapApiKey;

    @Enumerated(EnumType.STRING)
    private TemperatureUnit temperatureUnit;

    @Enumerated(EnumType.STRING)
    private SystemOfMeasurement systemOfMeasurement;

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
}
