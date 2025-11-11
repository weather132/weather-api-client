package com.github.yun531.climate.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LandForecast {
    private final Integer dayAfter;
    private final Integer popAm;
    private final Integer popPm;
}
