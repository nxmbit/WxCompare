package com.nxmbit.wxcompare.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class WeatherIconUtil {
    private static final String ICON_PATH = "/com/nxmbit/wxcompare/assets/weather/png/%s.png";

    public static ImageView createWeatherIcon(String iconCode, int size) {
        try {
            String path = String.format(ICON_PATH, iconCode);
            Image image = new Image(WeatherIconUtil.class.getResourceAsStream(path), size, size, true, true);
            return new ImageView(image);
        } catch (Exception e) {
            Image image = new Image(WeatherIconUtil.class.getResourceAsStream(String.format(ICON_PATH, "01d")),
                    size, size, true, true);
            return new ImageView(image);
        }
    }
}