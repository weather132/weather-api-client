package com.github.yun531.climate.weatherApi;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("weatherapi.short.grid")
@RequiredArgsConstructor
public class WeatherApiUrls {
    public final String SHORT_GRID_FORECAST;
}
