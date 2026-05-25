package com.github.yun531.climate.forecast.domain.readmodel;

public record FcstDailyPoint(
        int daysAhead,
        Integer minTemp,
        Integer maxTemp,
        Integer amPop,
        Integer pmPop
) {}