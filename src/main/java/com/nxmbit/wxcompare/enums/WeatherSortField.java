package com.nxmbit.wxcompare.enums;

public enum WeatherSortField {
    NAME("Name"),
    TEMPERATURE("Temperature"),
    HUMIDITY("Humidity"),
    PRESSURE("Pressure"),
    WIND_SPEED("Wind Speed"),
    CLOUDINESS("Cloudiness"),
    FEELS_LIKE("Feels Like"),
    MIN_TEMP("Min Temp"),
    MAX_TEMP("Max Temp"),
    VISIBILITY("Visibility"),
    RAIN_1H("Rain 1h"),
    SNOW_1H("Snow 1h");

    private final String displayName;

    WeatherSortField(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}