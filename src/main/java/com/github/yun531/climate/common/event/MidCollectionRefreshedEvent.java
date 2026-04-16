package com.github.yun531.climate.common.event;

import java.time.LocalDateTime;

/**
 * MidLand + MidTemperature 통합 수집 완료 이벤트.
 */
public record MidCollectionRefreshedEvent(LocalDateTime announceTime) {
}