package com.github.yun531.climate.common.event;

import java.time.LocalDateTime;

/**
 * AirQuality 수집 완료 이벤트.
 */
public record AirQualityRefreshedEvent(LocalDateTime announceTime) {
}