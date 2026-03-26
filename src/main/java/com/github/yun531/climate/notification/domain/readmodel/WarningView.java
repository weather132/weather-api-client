package com.github.yun531.climate.notification.domain.readmodel;

import com.github.yun531.climate.warning.domain.model.WarningEventType;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 기상특보 알림 전용 읽기 모델.
 */
public record WarningView(
        long eventId,
        WarningKind kind,
        WarningLevel level,
        @Nullable WarningLevel prevLevel,
        WarningEventType eventType,
        LocalDateTime announceTime,
        LocalDateTime effectiveTime
) {
    public WarningView {
        Objects.requireNonNull(kind, "kind must not be null");
        Objects.requireNonNull(level, "level must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(announceTime, "announceTime must not be null");
        Objects.requireNonNull(effectiveTime, "effectiveTime must not be null");
    }
}