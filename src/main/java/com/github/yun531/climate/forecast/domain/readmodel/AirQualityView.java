package com.github.yun531.climate.forecast.domain.readmodel;

import java.time.LocalDateTime;

public record AirQualityView(
        LocalDateTime announceTime,
        Integer pm10,
        String pm10Grade,
        Integer pm25,
        String pm25Grade
) {}