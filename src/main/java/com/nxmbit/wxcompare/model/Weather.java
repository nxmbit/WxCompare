package com.nxmbit.wxcompare.model;

import java.time.Instant;

public class Weather {
    private String main;
    private String description;
    private String iconCode;
    private double temperature;
    private double feelsLike;
    private double tempMin;
    private double tempMax;
    private int humidity;
    private int pressure;
    private double windSpeed;
    private int windDeg;
    private Instant timestamp;
    private int cloudinessPercent;
    private int rainMmLastHour = 0;
    private int snowMmLastHour = 0;
    private int visibilityInMeters;
    private Instant sunrise;
    private Instant sunset;

    public Weather() {}

    public String getMain() { return main; }
    public void setMain(String main) { this.main = main; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconCode() { return iconCode; }
    public void setIconCode(String iconCode) { this.iconCode = iconCode; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }

    public double getTempMin() { return tempMin; }
    public void setTempMin(double tempMin) { this.tempMin = tempMin; }

    public double getTempMax() { return tempMax; }
    public void setTempMax(double tempMax) { this.tempMax = tempMax; }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public int getPressure() { return pressure; }
    public void setPressure(int pressure) { this.pressure = pressure; }

    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

    public int getWindDeg() { return windDeg; }
    public void setWindDeg(int windDeg) { this.windDeg = windDeg; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public int getCloudinessPercent() { return cloudinessPercent; }
    public void setCloudinessPercent(int cloudinessPercent) { this.cloudinessPercent = cloudinessPercent; }

    public int getRainMmLastHour() { return rainMmLastHour; }
    public void setRainMmLastHour(int rainMmLastHour) { this.rainMmLastHour = rainMmLastHour; }

    public int getSnowMmLastHour() { return snowMmLastHour; }
    public void setSnowMmLastHour(int snowMmLastHour) { this.snowMmLastHour = snowMmLastHour; }

    public Instant getSunrise() { return sunrise; }
    public void setSunrise(Instant sunrise) { this.sunrise = sunrise; }

    public Instant getSunset() { return sunset; }
    public void setSunset(Instant sunset) { this.sunset = sunset; }

    public int getVisibilityInMeters() { return visibilityInMeters; }
    public void setVisibilityInMeters(int visibilityInMeters) { this.visibilityInMeters = visibilityInMeters; }
}