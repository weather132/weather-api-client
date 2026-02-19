package com.github.yun531.climate.weatherApi;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("weatherapi")
@RequiredArgsConstructor
public class WeatherApiUrls {
    public final String SHORT_GRID_FORECAST;
    public final String MID_TEMPERATURE_FORECAST;
    public final String MID_LAND_FORECAST;
}
