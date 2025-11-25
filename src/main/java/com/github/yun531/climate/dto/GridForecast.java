package com.github.yun531.climate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GridForecast {
    private final LocalDateTime announceTime;
    private final LocalDateTime effectiveTime;
    private final String forecastCategory;
    private final List<CoordsForecast> coordsForecastList;
}
