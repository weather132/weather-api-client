package com.github.yun531.climate.warning.contract;

import com.github.yun531.climate.warning.domain.model.WarningEventType;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;

import java.time.LocalDateTime;
import java.util.Objects;

public record IssuedWarning(
        long eventId,
        WarningKind kind,
        WarningLevel level,
        WarningLevel prevLevel,
        WarningEventType eventType,
        LocalDateTime announceTime,
        LocalDateTime effectiveTime
) {
    public IssuedWarning {
        Objects.requireNonNull(kind);
        Objects.requireNonNull(level);
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(announceTime);
        Objects.requireNonNull(effectiveTime);
    }
}