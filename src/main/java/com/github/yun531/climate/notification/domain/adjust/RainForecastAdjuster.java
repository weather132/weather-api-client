package com.github.yun531.climate.notification.domain.adjust;

import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload.DailyRainFlags;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload.RainInterval;
import com.github.yun531.climate.common.time.TimeShiftUtil;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.HOURS;

/**
 * RainForecast AlertEvent 시간 동기화 및 윈도우 클리핑
 * - Shift   : 발표 시각을 현재 시각으로 밀어 '최신성'을 유지 (최대 2시간)
 * - Clipping: 전체 데이터 중 [현재 시각 ~ 24시간 후] 범위만 추출
 * - Drop    : 날짜 경계가 지남에 따라 불필요해진 일별 데이터(DayParts) 제거
 * * 보장 조건: 스냅샷(26개) = Horizon(24개) + MaxShift(2개)
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

    /** 발표시각 기준 시프트 + 윈도우 클리핑을 적용한 AlertEvent를 반환 */
    public AlertEvent adjust(AlertEvent event, @Nullable LocalDateTime announceTime, LocalDateTime now) {
        if (event == null) return null;
        if (announceTime == null || now == null) return event;

        TimeShiftUtil.ShiftResult shift = TimeShiftUtil.shiftHourly(announceTime, now, maxShiftHours);

        if (!(event.payload() instanceof RainForecastPayload payload)) {
            throw new IllegalArgumentException(
                    "RainForecastAdjuster expects RainForecastPayload, got: "
                            + event.payload().getClass().getSimpleName());
        }

        LocalDateTime nowHour = now.truncatedTo(HOURS);
        List<RainInterval> clamped = clampToWindow(
                payload.hourlyParts(),
                nowHour.plusHours(startOffsetHours),
                nowHour.plusHours(windowHours));

        List<DailyRainFlags> newDays = shiftDayParts(payload.dayParts(), shift.dayShift());

        RainForecastPayload newPayload = new RainForecastPayload(clamped, newDays);
        return withShiftedTime(event, shift.shiftedBaseTime(), newPayload);
    }

    // =====================================================================
    //  hourlyParts 윈도우 클리핑: 밖이면 제거, 걸치면 경계로 잘라서 보존
    // =====================================================================

    private List<RainInterval> clampToWindow(
            List<RainInterval> parts, LocalDateTime start, LocalDateTime end
    ) {
        if (parts == null || parts.isEmpty()) return List.of();

        List<RainInterval> out = new ArrayList<>(parts.size());
        for (RainInterval r : parts) {
            RainInterval clamped = clampInterval(r, start, end);
            if (clamped != null) out.add(clamped);
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    @Nullable
    private RainInterval clampInterval(RainInterval r, LocalDateTime start, LocalDateTime end) {
        if (r == null || r.start() == null || r.end() == null) return null;
        if (r.end().isBefore(start) || r.start().isAfter(end)) return null;

        LocalDateTime cStart = r.start().isBefore(start) ? start : r.start();
        LocalDateTime cEnd = r.end().isAfter(end) ? end : r.end();
        return cEnd.isBefore(cStart) ? null : new RainInterval(cStart, cEnd);
    }

    // =====================================================================
    //  dayParts 시프트: 앞쪽 드롭 + 뒤쪽 빈 플래그 패딩
    // =====================================================================

    private List<DailyRainFlags> shiftDayParts(List<DailyRainFlags> days, int dayShift) {
        if (days == null || days.isEmpty()) return List.of();
        if (dayShift <= 0) return days;

        int n = days.size();
        List<DailyRainFlags> out = new ArrayList<>(n);

        for (int i = dayShift; i < n; i++) {
            DailyRainFlags v = days.get(i);
            out.add(v != null ? v : EMPTY_FLAGS);
        }
        while (out.size() < n) {
            out.add(EMPTY_FLAGS);
        }

        return List.copyOf(out);
    }

    //  --- AlertEvent 재조립 헬퍼 ---

    private AlertEvent withShiftedTime(AlertEvent event, LocalDateTime shiftedTime, RainForecastPayload payload) {
        return new AlertEvent(event.type(), event.regionId(), shiftedTime, payload);
    }
}