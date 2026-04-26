package com.github.yun531.climate.notification.domain.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.yun531.climate.warning.domain.model.WarningEventType;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

/**
 * 기상특보 발령 알림 페이로드.
 * - level: 특보 단계 (WarningEventType)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WarningIssuedPayload(
        String kind,
        String level,
        @Nullable String prevLevel,
        String eventType,
        long eventId,
        LocalDateTime effectiveTime
) implements AlertPayload {}