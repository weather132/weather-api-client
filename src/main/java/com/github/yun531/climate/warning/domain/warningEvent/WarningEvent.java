package com.github.yun531.climate.warning.domain.warningEvent;

import com.github.yun531.climate.warning.domain.shared.WarningEventType;
import com.github.yun531.climate.warning.domain.shared.WarningKind;
import com.github.yun531.climate.warning.domain.shared.WarningLevel;
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
import java.util.Objects;

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

    private WarningEvent(String warningRegionCode, WarningKind kind,
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

    public static WarningEvent issued(WarningCurrent current) {
        Objects.requireNonNull(current);
        return new WarningEvent(
                current.warningRegionCode(), current.kind(),
                current.level(), null,
                WarningEventType.NEW,
                current.announceTime(), current.effectiveTime()
        );
    }

    public static WarningEvent upgraded(WarningCurrent current, WarningLevel prevLevel) {
        Objects.requireNonNull(current);
        Objects.requireNonNull(prevLevel);
        return new WarningEvent(
                current.warningRegionCode(), current.kind(),
                current.level(), prevLevel,
                WarningEventType.UPGRADED,
                current.announceTime(), current.effectiveTime()
        );
    }

    public static WarningEvent downgraded(WarningCurrent current, WarningLevel prevLevel) {
        Objects.requireNonNull(current);
        Objects.requireNonNull(prevLevel);
        return new WarningEvent(
                current.warningRegionCode(), current.kind(),
                current.level(), prevLevel,
                WarningEventType.DOWNGRADED,
                current.announceTime(), current.effectiveTime()
        );
    }

    public static WarningEvent extended(WarningCurrent current) {
        Objects.requireNonNull(current);
        return new WarningEvent(
                current.warningRegionCode(), current.kind(),
                current.level(), null,
                WarningEventType.EXTENDED,
                current.announceTime(), current.effectiveTime()
        );
    }

    public static WarningEvent lifted(WarningCurrent removed, LocalDateTime detectedAt) {
        Objects.requireNonNull(removed);
        Objects.requireNonNull(detectedAt);
        return new WarningEvent(
                removed.warningRegionCode(), removed.kind(),
                removed.level(), null,
                WarningEventType.LIFTED,
                detectedAt, detectedAt
        );
    }

    public boolean isActive() {
        return eventType != WarningEventType.LIFTED;
    }
}