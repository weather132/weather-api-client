package com.github.yun531.climate.notification.domain.adjust;

import com.github.yun531.climate.common.time.TimeShiftUtil;
import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload.DailyRainFlags;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload.RainInterval;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.HOURS;

/**
 * RainForecast AlertEvent의 시간 동기화 및 윈도우 클리핑을 담당.
 * - Shift   : 발표 시각을 현재 시각으로 밀어 '최신성'을 유지 (최대 maxShiftHours(2))
 * - Clipping: 전체 데이터 중 [현재 시각 + startOffset ~ windowHours(24) 후] 범위만 추출
 * - Drop    : 날짜 경계가 지남에 따라 불필요해진 일별 데이터(dayParts) 제거
 * 보장 조건: 스냅샷(26개) = Horizon(24개) + MaxShift(2개)
 */
public class RainForecastAdjuster {

    private static final DailyRainFlags EMPTY_FLAGS = new DailyRainFlags(false, false);

    private final int maxShiftHours;
    private final int windowHours;
    private final int startOffsetHours;

    public RainForecastAdjuster(int maxShiftHours, int windowHours, int startOffsetHours) {
        this.maxShiftHours    = Math.max(0, maxShiftHours);
        this.windowHours      = Math.max(1, windowHours);
        this.startOffsetHours = Math.max(0, startOffsetHours);
    }

    /** 발표시각 기준 시프트 + 윈도우 클리핑을 적용한 AlertEvent를 반환. */
    public AlertEvent adjust(AlertEvent event, @Nullable LocalDateTime announceTime, LocalDateTime now) {
        if (event == null) {
            return null;
        }
        if (announceTime == null || now == null) {
            return event;
        }

        RainForecastPayload payload = rainForecastPayloadFrom(event);
        TimeShiftUtil.ShiftResult shiftedAt = TimeShiftUtil.shiftHourly(announceTime, now, maxShiftHours);

        RainForecastPayload adjustedPayload = adjustPayload(payload, shiftedAt, now);
        return adjustedEvent(event, shiftedAt.shiftedBaseTime(), adjustedPayload);
    }

    private RainForecastPayload rainForecastPayloadFrom(AlertEvent event) {
        if (event.payload() instanceof RainForecastPayload payload) {
            return payload;
        }
        throw new IllegalArgumentException(
                "RainForecastAdjuster expects RainForecastPayload, got: "
                        + event.payload().getClass().getSimpleName());
    }

    private RainForecastPayload adjustPayload(
            RainForecastPayload payload, TimeShiftUtil.ShiftResult shift, LocalDateTime now
    ) {
        List<RainInterval> clampedHours = clampToWindow(payload.hourlyParts(), windowStart(now), windowEnd(now));
        List<DailyRainFlags> shiftedDays = shiftDayParts(payload.dayParts(), shift.dayShift());
        return new RainForecastPayload(clampedHours, shiftedDays);
    }

    private LocalDateTime windowStart(LocalDateTime now) {
        return now.truncatedTo(HOURS).plusHours(startOffsetHours);
    }

    private LocalDateTime windowEnd(LocalDateTime now) {
        return now.truncatedTo(HOURS).plusHours(windowHours);
    }

    // hourlyParts 윈도우 클리핑: 밖이면 제거, 걸치면 경계로 잘라서 보존

    private List<RainInterval> clampToWindow(
            List<RainInterval> intervals, LocalDateTime windowStart, LocalDateTime windowEnd
    ) {
        if (intervals == null || intervals.isEmpty()) {
            return List.of();
        }
        List<RainInterval> clamped = new ArrayList<>(intervals.size());
        for (RainInterval interval : intervals) {
            RainInterval bounded = clampInterval(interval, windowStart, windowEnd);
            if (bounded != null) {
                clamped.add(bounded);
            }
        }
        return List.copyOf(clamped);
    }

    @Nullable
    private RainInterval clampInterval(RainInterval interval, LocalDateTime windowStart, LocalDateTime windowEnd) {
        if (interval == null || interval.start() == null || interval.end() == null) {
            return null;
        }
        if (isOutsideWindow(interval, windowStart, windowEnd)) {
            return null;
        }
        LocalDateTime boundedStart = laterOf(interval.start(), windowStart);
        LocalDateTime boundedEnd = earlierOf(interval.end(), windowEnd);
        return boundedEnd.isBefore(boundedStart) ? null : new RainInterval(boundedStart, boundedEnd);
    }

    private boolean isOutsideWindow(RainInterval interval, LocalDateTime windowStart, LocalDateTime windowEnd) {
        return interval.end().isBefore(windowStart) || interval.start().isAfter(windowEnd);
    }

    private LocalDateTime laterOf(LocalDateTime left, LocalDateTime right) {
        return left.isBefore(right) ? right : left;
    }

    private LocalDateTime earlierOf(LocalDateTime left, LocalDateTime right) {
        return left.isAfter(right) ? right : left;
    }

    // dayParts 시프트: 앞쪽 드롭 + 뒤쪽 빈 플래그 패딩

    private List<DailyRainFlags> shiftDayParts(List<DailyRainFlags> days, int dayShift) {
        if (days == null || days.isEmpty()) {
            return List.of();
        }
        if (dayShift <= 0) {
            return days;
        }
        int total = days.size();
        List<DailyRainFlags> shifted = new ArrayList<>(total);
        for (int day = dayShift; day < total; day++) {
            shifted.add(flagsOrEmpty(days.get(day)));
        }
        padWithEmptyFlags(shifted, total);
        return List.copyOf(shifted);
    }

    private void padWithEmptyFlags(List<DailyRainFlags> days, int targetSize) {
        while (days.size() < targetSize) {
            days.add(EMPTY_FLAGS);
        }
    }

    private DailyRainFlags flagsOrEmpty(@Nullable DailyRainFlags flags) {
        return flags != null ? flags : EMPTY_FLAGS;
    }

    private AlertEvent adjustedEvent(AlertEvent event, LocalDateTime shiftedTime, RainForecastPayload payload) {
        return new AlertEvent(event.type(), event.regionId(), shiftedTime, payload);
    }
}