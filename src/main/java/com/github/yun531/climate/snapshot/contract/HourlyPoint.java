package com.github.yun531.climate.snapshot.contract;

import java.time.LocalDateTime;

public record HourlyPoint(
        LocalDateTime effectiveTime,
        Integer temp,
        Integer pop
) {}