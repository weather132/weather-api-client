package com.github.yun531.climate.common.event;

import java.time.LocalDateTime;

/**
 * WarningEvent 수집 완료 이벤트.
 * warningEvents가 1건 이상 저장된 경우에만 발행.
 */
public record WarningRefreshedEvent(LocalDateTime announceTime) {
}