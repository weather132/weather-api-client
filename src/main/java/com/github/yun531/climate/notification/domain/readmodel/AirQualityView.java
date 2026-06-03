package com.github.yun531.climate.notification.domain.readmodel;

import java.time.LocalDateTime;

/**
 * notification 자체 미세먼지 view.
 * 측정 불가 시 모든 필드 null (빈 view).
 */
public record AirQualityView(
        LocalDateTime announceTime,
        Integer pm10,
        Integer pm25
) {}