package com.github.yun531.climate.notification.domain.payload;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 강수 이벤트 페이로드.
 * - effectiveTime: 비가 예보된 절대 시각
 * - pop: 해당 시각의 강수확률
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RainOnsetPayload(
        LocalDateTime effectiveTime,
        int pop
) implements AlertPayload {}