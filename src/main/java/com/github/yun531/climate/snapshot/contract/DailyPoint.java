package com.github.yun531.climate.snapshot.contract;

public record DailyPoint(
        int dayOffset,
        Integer minTemp,
        Integer maxTemp,
        Integer amPop,
        Integer pmPop
) {}