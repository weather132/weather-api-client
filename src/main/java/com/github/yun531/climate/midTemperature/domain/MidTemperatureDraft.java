package com.github.yun531.climate.midTemperature.domain;

import java.time.LocalDateTime;

public record MidTemperatureDraft(
        LocalDateTime effectiveTime,
        Integer maxTemp,
        Integer minTemp
) {}