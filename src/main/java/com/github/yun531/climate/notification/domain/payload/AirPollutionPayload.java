package com.github.yun531.climate.notification.domain.payload;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 미세먼지 알림 페이로드.
 * - pollutionType: PM10 / PM25
 * - value: 측정 수치
 *  * - grade: 임계 평가 결과 등급 (BAD / VERY_BAD)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AirPollutionPayload(
        String pollutionType,
        int value,
        String grade,
        LocalDateTime announceTime
) implements AlertPayload {}
