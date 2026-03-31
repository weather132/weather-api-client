package com.github.yun531.climate.warning.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@IdClass(WarningId.class)
@Table(name = "warning_current")
public class WarningCurrent {

    @Id
    @Column(name = "warning_region_code", nullable = false, length = 16)
    private String warningRegionCode;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 20)
    private WarningKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 12)
    private WarningLevel level;

    @Column(name = "announce_time", nullable = false)
    private LocalDateTime announceTime;

    @Column(name = "effective_time", nullable = false)
    private LocalDateTime effectiveTime;

    public WarningCurrent(String warningRegionCode, WarningKind kind,
                          WarningLevel level, LocalDateTime announceTime,
                          LocalDateTime effectiveTime) {
        this.warningRegionCode = warningRegionCode;
        this.kind = kind;
        this.level = level;
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
    }

    public WarningEvent toEvent(WarningLevel prevLevel, WarningEventType eventType) {
        return new WarningEvent(warningRegionCode, kind, level, prevLevel,
                eventType, announceTime, effectiveTime);
    }
}