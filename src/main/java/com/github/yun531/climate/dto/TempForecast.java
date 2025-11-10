package com.github.yun531.climate.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TempForecast {
    private final Integer dayAfter;
    private final Integer minTemp;
    private final Integer maxTemp;
}
