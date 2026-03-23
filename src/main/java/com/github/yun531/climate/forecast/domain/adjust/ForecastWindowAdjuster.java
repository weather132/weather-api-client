package com.github.yun531.climate.forecast.domain.adjust;

import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyPoint;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyView;
import com.github.yun531.climate.common.time.TimeShiftUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * now 기준으로 시간대별 예보를 windowSize 개의 슬라이딩 윈도우로 재구성.
 * announceTime이 최대 maxShiftHours 만큼 과거여도 유효한 것으로 간주하며,
 * effectiveTime > shiftedAnnounceTime인 포인트만 취한다.
 */
public final class ForecastWindowAdjuster {

    private static final Comparator<ForecastHourlyPoint> BY_VALID_AT =
            Comparator.comparing(ForecastHourlyPoint::effectiveTime,
                    Comparator.nullsLast(Comparator.naturalOrder()));

    private final int maxShiftHours;
    private final int windowSize;

    public ForecastWindowAdjuster(int maxShiftHours, int windowSize) {
        if (maxShiftHours < 0) throw new IllegalArgumentException("maxShiftHours must be >= 0");
        if (windowSize <= 0) throw new IllegalArgumentException("windowSize must be > 0");
        this.maxShiftHours = maxShiftHours;
        this.windowSize = windowSize;
    }

    /** now 기준으로 시프트 + 윈도우 절단을 적용한 ForecastHourlyView를 반환 */
    public ForecastHourlyView adjust(ForecastHourlyView base, LocalDateTime now) {
        if (base == null) return null;

        List<ForecastHourlyPoint> sorted = sortByEffectiveTime(base.hourlyPoints());

        LocalDateTime announceTime = base.announceTime();
        if (announceTime == null || now == null || sorted.isEmpty()) {
            return new ForecastHourlyView(base.regionId(), announceTime, sorted);
        }

        TimeShiftUtil.ShiftResult shift = TimeShiftUtil.shiftHourly(announceTime, now, maxShiftHours);
        LocalDateTime shiftedTime = (shift.shiftHours() <= 0) ? announceTime : shift.shiftedBaseTime();

        List<ForecastHourlyPoint> window = filterByWindow(sorted, shiftedTime);
        return new ForecastHourlyView(base.regionId(), shiftedTime, window);
    }

    /** shiftedTime 초과(>) 포인트만 남기고, 최대 windowSize 개로 절단 */
    private List<ForecastHourlyPoint> filterByWindow(
            List<ForecastHourlyPoint> sorted, LocalDateTime shiftedTime
    ) {
        List<ForecastHourlyPoint> out = new ArrayList<>(windowSize);

        for (ForecastHourlyPoint p : sorted) {
            if (p == null || p.effectiveTime() == null) continue;
            if (!p.effectiveTime().isAfter(shiftedTime)) continue;

            out.add(p);
            if (out.size() == windowSize) break;
        }

        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    /** effectiveTime 기준 정렬 + null 제거 */
    private List<ForecastHourlyPoint> sortByEffectiveTime(List<ForecastHourlyPoint> src) {
        if (src == null || src.isEmpty()) return List.of();
        return src.stream()
                .filter(Objects::nonNull)
                .sorted(BY_VALID_AT)
                .toList();
    }
}