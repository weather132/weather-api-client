package com.github.yun531.climate.forecast.domain.adjust;

import com.github.yun531.climate.common.time.TimeShiftUtil;
import com.github.yun531.climate.forecast.domain.readmodel.FcstHourlyPoint;
import com.github.yun531.climate.forecast.domain.readmodel.FcstHourlyView;

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
public final class FcstWindowAdjuster {

    private static final Comparator<FcstHourlyPoint> BY_EFFECTIVE_TIME =
            Comparator.comparing(FcstHourlyPoint::effectiveTime,
                    Comparator.nullsLast(Comparator.naturalOrder()));

    private final int maxShiftHours;
    private final int windowSize;

    public FcstWindowAdjuster(int maxShiftHours, int windowSize) {
        if (maxShiftHours < 0) {
            throw new IllegalArgumentException("maxShiftHours must be >= 0");
        }
        if (windowSize <= 0) {
            throw new IllegalArgumentException("windowSize must be > 0");
        }
        this.maxShiftHours = maxShiftHours;
        this.windowSize = windowSize;
    }

    /** now 기준으로 시프트 + 윈도우 절단을 적용한 FcstHourlyView를 반환. */
    public FcstHourlyView adjust(FcstHourlyView base, LocalDateTime now) {
        if (base == null) {
            return null;
        }

        List<FcstHourlyPoint> sortedPoints = sortByEffectiveTime(base.hourlyPoints());
        LocalDateTime announceTime = base.announceTime();
        if (cannotShift(announceTime, now, sortedPoints)) {
            return adjustedView(base, announceTime, sortedPoints);
        }

        LocalDateTime shiftedTime = shiftedTimeOf(announceTime, now);
        List<FcstHourlyPoint> windowedPoints = collectWithinWindow(sortedPoints, shiftedTime);
        return adjustedView(base, shiftedTime, windowedPoints);
    }

    private boolean cannotShift(LocalDateTime announceTime, LocalDateTime now, List<FcstHourlyPoint> sortedPoints) {
        return announceTime == null || now == null || sortedPoints.isEmpty();
    }

    private LocalDateTime shiftedTimeOf(LocalDateTime announceTime, LocalDateTime now) {
        TimeShiftUtil.ShiftResult shift = TimeShiftUtil.shiftHourly(announceTime, now, maxShiftHours);
        return shift.shiftHours() <= 0 ? announceTime : shift.shiftedBaseTime();
    }

    /** shiftedTime 초과(>) 포인트만 남기고, 최대 windowSize개로 절단. */
    private List<FcstHourlyPoint> collectWithinWindow(List<FcstHourlyPoint> sortedPoints, LocalDateTime shiftedTime) {
        List<FcstHourlyPoint> windowedPoints = new ArrayList<>(windowSize);
        for (FcstHourlyPoint point : sortedPoints) {
            if (point == null || point.effectiveTime() == null) {
                continue;
            }
            if (!isAfterShift(point, shiftedTime)) {
                continue;
            }
            windowedPoints.add(point);
            if (windowedPoints.size() == windowSize) {
                break;
            }
        }
        return List.copyOf(windowedPoints);
    }

    private boolean isAfterShift(FcstHourlyPoint point, LocalDateTime shiftedTime) {
        return point.effectiveTime().isAfter(shiftedTime);
    }

    /** effectiveTime 기준 정렬 + null 제거 */
    private List<FcstHourlyPoint> sortByEffectiveTime(List<FcstHourlyPoint> points) {
        if (points == null || points.isEmpty()) {
            return List.of();
        }
        return points.stream()
                .filter(Objects::nonNull)
                .sorted(BY_EFFECTIVE_TIME)
                .toList();
    }

    private FcstHourlyView adjustedView(FcstHourlyView base, LocalDateTime announceTime, List<FcstHourlyPoint> points) {
        return new FcstHourlyView(base.regionId(), announceTime, points);
    }
}