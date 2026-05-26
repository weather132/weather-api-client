package com.github.yun531.climate.forecast.domain.readmodel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 시간대별 예보 DTO
 * - hourlyPoints: (announceTime, temp, pop)
 */
public record FcstHourlyView(
        String regionId,
        LocalDateTime announceTime,
        List<FcstHourlyPoint> hourlyPoints
) {
    public FcstHourlyView {
        Objects.requireNonNull(hourlyPoints, "hourlyPoints must not be null");
        hourlyPoints = List.copyOf(hourlyPoints);
    }
}