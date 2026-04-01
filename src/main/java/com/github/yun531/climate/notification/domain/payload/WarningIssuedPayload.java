package com.github.yun531.climate.notification.domain.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.yun531.climate.warning.domain.model.WarningEventType;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WarningIssuedPayload(
        WarningKind kind,
        WarningLevel level,
        @Nullable WarningLevel prevLevel,
        WarningEventType eventType,
        long eventId,
        LocalDateTime effectiveTime
) implements AlertPayload {}