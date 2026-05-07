package com.github.yun531.climate.warning.domain.warningEvent;

import com.github.yun531.climate.warning.domain.shared.WarningKind;
import com.github.yun531.climate.warning.domain.shared.WarningLevel;

import java.time.LocalDateTime;
import java.util.Objects;

public record WarningCurrent(
        String warningRegionCode,
        WarningKind kind,
        WarningLevel level,
        LocalDateTime announceTime,
        LocalDateTime effectiveTime
) {
    public WarningCurrent {
        Objects.requireNonNull(warningRegionCode);
        Objects.requireNonNull(kind);
        Objects.requireNonNull(level);
        Objects.requireNonNull(announceTime);
        Objects.requireNonNull(effectiveTime);
    }
}