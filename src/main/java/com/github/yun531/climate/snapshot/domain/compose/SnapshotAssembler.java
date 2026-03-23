package com.github.yun531.climate.snapshot.domain.compose;

import com.github.yun531.climate.snapshot.contract.DailyPoint;
import com.github.yun531.climate.snapshot.contract.HourlyPoint;
import com.github.yun531.climate.snapshot.contract.WeatherSnapshot;
import com.github.yun531.climate.snapshot.domain.model.DailyForecastItem;
import com.github.yun531.climate.snapshot.domain.model.HourlyForecastItem;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * HourlyForecastItem + DailyForecastItem -> WeatherSnapshot 조립.
 */
@Component
public class SnapshotAssembler {

    private static final int MAX_HOURLY_POINTS = 26;
    private static final int DAILY_RANGE = 7;

    public WeatherSnapshot assemble(
            String regionId,
            LocalDateTime announceTime,
            List<HourlyForecastItem> hourlyItems,
            List<DailyForecastItem> dailyItems
    ) {
        LocalDate baseDate = announceTime.toLocalDate();

        return new WeatherSnapshot(
                regionId,
                announceTime,
                toHourlyPoints(hourlyItems),
                toDailyPoints(dailyItems, baseDate)
        );
    }

    // =====================================================================
    //  Hourly: HourlyForecastItem -> HourlyPoint (최대 26개)
    // =====================================================================

    private List<HourlyPoint> toHourlyPoints(List<HourlyForecastItem> items) {
        if (items == null || items.isEmpty()) return List.of();

        return items.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getEffectiveTime() != null)
                .sorted(Comparator.comparing(HourlyForecastItem::getEffectiveTime))
                .limit(MAX_HOURLY_POINTS)
                .map(item -> new HourlyPoint(
                        item.getEffectiveTime(),
                        item.getTemp(),
                        item.getPop()))
                .toList();
    }

    // =====================================================================
    //  Daily: DailyForecastItem -> DailyPoint 7개 (daysAhead 0~6)
    // =====================================================================

    private List<DailyPoint> toDailyPoints(
            List<DailyForecastItem> items, LocalDate baseDate
    ) {
        if (baseDate == null || items == null || items.isEmpty()) {
            return emptyDailyPoints();
        }

        Map<Integer, List<DailyForecastItem>> grouped = groupByDaysAhead(baseDate, items);

        DailyPoint[] points = new DailyPoint[DAILY_RANGE];
        for (int daysAhead = 0; daysAhead < DAILY_RANGE; daysAhead++) {
            List<DailyForecastItem> dayItems = grouped.getOrDefault(daysAhead, List.of());
            points[daysAhead] = aggregateDay(daysAhead, dayItems);
        }

        return List.of(points);
    }

    private Map<Integer, List<DailyForecastItem>> groupByDaysAhead(
            LocalDate baseDate, List<DailyForecastItem> items
    ) {
        Map<Integer, List<DailyForecastItem>> grouped = new HashMap<>();

        for (DailyForecastItem item : items) {
            if (item == null || item.getEffectiveTime() == null) continue;

            int daysAhead = (int) ChronoUnit.DAYS.between(
                    baseDate, item.getEffectiveTime().toLocalDate());
            if (daysAhead < 0 || daysAhead >= DAILY_RANGE) continue;

            grouped.computeIfAbsent(daysAhead, k -> new ArrayList<>()).add(item);
        }

        return grouped;
    }

    private DailyPoint aggregateDay(int daysAhead, List<DailyForecastItem> items) {
        Integer minTemp = null, maxTemp = null;
        Integer amPop = null, pmPop = null;

        for (DailyForecastItem item : items) {
            Integer temp = item.getTemp();
            if (temp != null) {
                minTemp = (minTemp == null) ? temp : Math.min(minTemp, temp);
                maxTemp = (maxTemp == null) ? temp : Math.max(maxTemp, temp);
            }

            Integer pop = item.getPop();
            if (pop != null) {
                if (item.getEffectiveTime().getHour() < 12) {
                    amPop = (amPop == null) ? pop : Math.max(amPop, pop);
                } else {
                    pmPop = (pmPop == null) ? pop : Math.max(pmPop, pop);
                }
            }
        }

        return new DailyPoint(daysAhead, minTemp, maxTemp, amPop, pmPop);
    }

    private List<DailyPoint> emptyDailyPoints() {
        DailyPoint[] points = new DailyPoint[DAILY_RANGE];
        for (int i = 0; i < DAILY_RANGE; i++) {
            points[i] = new DailyPoint(i, null, null, null, null);
        }
        return List.of(points);
    }
}