package com.github.yun531.climate.warning.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "warning_event",
        indexes = @Index(name = "idx_wrn_reg_kind_id",
                columnList = "warning_region_code, kind, id")
)
public class WarningEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warning_region_code", nullable = false, length = 16)
    private String warningRegionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 20)
    private WarningKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 12)
    private WarningLevel level;

    @Enumerated(EnumType.STRING)
    @Column(name = "prev_level", length = 12)
    private WarningLevel prevLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 12)
    private WarningEventType eventType;

    @Column(name = "announce_time", nullable = false)
    private LocalDateTime announceTime;

    @Column(name = "effective_time", nullable = false)
    private LocalDateTime effectiveTime;

    public WarningEvent(String warningRegionCode, WarningKind kind,
                        WarningLevel level, WarningLevel prevLevel,
                        WarningEventType eventType,
                        LocalDateTime announceTime, LocalDateTime effectiveTime) {
        this.warningRegionCode = warningRegionCode;
        this.kind = kind;
        this.level = level;
        this.prevLevel = prevLevel;
        this.eventType = eventType;
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
    }

    public boolean isActive() {
        return eventType != WarningEventType.LIFTED;
    }
}