package com.github.yun531.climate.common.event;

import java.time.LocalDateTime;

/**
 * ShortLand 수집 완료 이벤트.
 */
public record ShortLandRefreshedEvent(LocalDateTime announceTime) {
}