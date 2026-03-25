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

    @Column(name = "tm_fc", nullable = false)
    private LocalDateTime tmFc;

    @Column(name = "tm_ef", nullable = false)
    private LocalDateTime tmEf;

    public WarningCurrent(String warningRegionCode, WarningKind kind,
                          WarningLevel level, LocalDateTime tmFc, LocalDateTime tmEf) {
        this.warningRegionCode = warningRegionCode;
        this.kind = kind;
        this.level = level;
        this.tmFc = tmFc;
        this.tmEf = tmEf;
    }

    public WarningEvent toEvent(WarningLevel prevLevel, WarningEventType eventType) {
        return new WarningEvent(warningRegionCode, kind, level, prevLevel, eventType, tmFc, tmEf);
    }
}