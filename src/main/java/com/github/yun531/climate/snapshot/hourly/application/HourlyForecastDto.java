package com.github.yun531.climate.snapshot.hourly.application;

import lombok.Getter;

import java.util.List;

@Getter
public class HourlyForecastDto {
    private final String announceTime;
    private final Integer coordsX;
    private final Integer coordsY;
    private final List<HourlyForecastData> gridForecastData;

    public HourlyForecastDto(String announceTime, Integer coordsX, Integer coordsY, List<HourlyForecastData> gridForecastData) {
        this.announceTime = announceTime;
        this.coordsX = coordsX;
        this.coordsY = coordsY;
        this.gridForecastData = gridForecastData;
    }
}
