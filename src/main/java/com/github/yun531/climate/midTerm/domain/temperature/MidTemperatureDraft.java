package com.github.yun531.climate.midTerm.domain.temperature;

import java.time.LocalDateTime;

public record MidTemperatureDraft(
        LocalDateTime effectiveTime,
        Integer maxTemp,
        Integer minTemp
) {}