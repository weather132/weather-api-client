package com.github.yun531.climate.forecast.domain.compose;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastDailyPoint;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandRepository;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureRepository;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCodeRepository;
import com.github.yun531.climate.shortLand.domain.ShortLand;
import com.github.yun531.climate.shortLand.domain.ShortLandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * ShortLand + MidLand/MidTemperature fallback → ForecastDailyPoint 직접 생산.
 */
@Component
@RequiredArgsConstructor
public class DailyForecastComposer {

    private static final int DAILY_RANGE = 7;

    private final ShortLandRepository shortLandRepository;
    private final MidLandRepository midLandRepository;
    private final MidTemperatureRepository midTemperatureRepository;
    private final ProvinceRegionCodeRepository provinceRegionCodeRepository;

    public record DailyComposeResult(
            LocalDateTime announceTime,
            List<ForecastDailyPoint> forecastDailyPoints
    ) {}

    public DailyComposeResult compose(CityRegionCode cityRegionCode) {
        List<LocalDateTime> effectiveTimes = getEffectiveTimes(LocalDateTime.now());

        // ShortLand 배치 조회 (1회 쿼리)
        Map<LocalDateTime, ShortLand> shortLandItems =
                shortLandRepository.findRecentAll(cityRegionCode, effectiveTimes);

        // ShortLand 없는 시간대 분류
        List<LocalDateTime> missingTimes = effectiveTimes.stream()
                .filter(et -> !shortLandItems.containsKey(et))
                .toList();

        // Mid fallback 배치 조회 (최대 3회 쿼리)
        MidBatchResult midResult = missingTimes.isEmpty()
                ? new MidBatchResult(Map.of(), null)
                : composeMidBatch(cityRegionCode, missingTimes);

        // 순서 유지하며 DailyRawItem 조립
        List<DailyRawItem> rawItems = new ArrayList<>(effectiveTimes.size());
        LocalDateTime shortLandAnnounceTime = null;

        for (LocalDateTime effectiveTime : effectiveTimes) {
            ShortLand shortLand = shortLandItems.get(effectiveTime);
            if (shortLand != null) {
                if (shortLandAnnounceTime == null) {
                    shortLandAnnounceTime = shortLand.getAnnounceTime();
                }

                Integer pop = shortLand.getPop();
                Integer temp = shortLand.getTemp();

                if (pop == null) {
                    pop = shortLandRepository.findRecentPop(cityRegionCode, effectiveTime);
                }
                if (temp == null) {
                    temp = isMorning(effectiveTime)
                            ? shortLandRepository.findRecentMinTemp(cityRegionCode, effectiveTime)
                            : shortLandRepository.findRecentMaxTemp(cityRegionCode, effectiveTime);
                }

                rawItems.add(new DailyRawItem(effectiveTime, temp, pop));
            } else {
                DailyRawItem midItem = midResult.items().get(effectiveTime);
                if (midItem != null) {
                    rawItems.add(midItem);
                }
            }
        }

        // announceTime: ShortLand 우선, 없으면 Mid fallback
        LocalDateTime announceTime = (shortLandAnnounceTime != null)
                ? shortLandAnnounceTime
                : midResult.announceTime();

        // DailyRawItem → ForecastDailyPoint 집계 (기존 SnapshotAssembler 로직 흡수)
        LocalDate baseDate = (announceTime != null)
                ? announceTime.toLocalDate()
                : LocalDateTime.now().toLocalDate();
        List<ForecastDailyPoint> dailyPoints = aggregate(baseDate, rawItems);

        return new DailyComposeResult(announceTime, dailyPoints);
    }


    // ── 집계 로직 ──────────────────────

    private List<ForecastDailyPoint> aggregate(LocalDate baseDate, List<DailyRawItem> rawItems) {
        if (rawItems.isEmpty()) {
            return emptyDailyPoints();
        }

        Map<Integer, List<DailyRawItem>> grouped = groupByDaysAhead(baseDate, rawItems);

        List<ForecastDailyPoint> points = new ArrayList<>(DAILY_RANGE);
        for (int daysAhead = 0; daysAhead < DAILY_RANGE; daysAhead++) {
            List<DailyRawItem> dayItems = grouped.getOrDefault(daysAhead, List.of());
            points.add(aggregateDay(daysAhead, dayItems));
        }
        return points;
    }

    private Map<Integer, List<DailyRawItem>> groupByDaysAhead(
            LocalDate baseDate, List<DailyRawItem> items
    ) {
        Map<Integer, List<DailyRawItem>> grouped = new HashMap<>();

        for (DailyRawItem item : items) {
            if (item.effectiveTime() == null) continue;

            int daysAhead = (int) ChronoUnit.DAYS.between(
                    baseDate, item.effectiveTime().toLocalDate());
            if (daysAhead < 0 || daysAhead >= DAILY_RANGE) continue;

            grouped.computeIfAbsent(daysAhead, k -> new ArrayList<>()).add(item);
        }
        return grouped;
    }

    private ForecastDailyPoint aggregateDay(int daysAhead, List<DailyRawItem> items) {
        Integer minTemp = null, maxTemp = null;
        Integer amPop = null, pmPop = null;

        for (DailyRawItem item : items) {
            Integer temp = item.temp();
            if (temp != null) {
                minTemp = (minTemp == null) ? temp : Math.min(minTemp, temp);
                maxTemp = (maxTemp == null) ? temp : Math.max(maxTemp, temp);
            }

            Integer pop = item.pop();
            if (pop != null) {
                if (item.effectiveTime().getHour() < 12) {
                    amPop = (amPop == null) ? pop : Math.max(amPop, pop);
                } else {
                    pmPop = (pmPop == null) ? pop : Math.max(pmPop, pop);
                }
            }
        }
        return new ForecastDailyPoint(daysAhead, minTemp, maxTemp, amPop, pmPop);
    }

    private List<ForecastDailyPoint> emptyDailyPoints() {
        List<ForecastDailyPoint> points = new ArrayList<>(DAILY_RANGE);
        for (int i = 0; i < DAILY_RANGE; i++) {
            points.add(new ForecastDailyPoint(i, null, null, null, null));
        }
        return points;
    }

    // ── Mid fallback 배치 조회 ──────────────────────────────────────────

    /**
     * Mid fallback 배치: ProvinceRegionCode 1회 + MidTemperature 1회 + MidLand 1회.
     * announceTime을 MidLand에서 추출하여 함께 반환.
     */
    private MidBatchResult composeMidBatch(
            CityRegionCode cityRegionCode, List<LocalDateTime> missingTimes
    ) {
        ProvinceRegionCode provinceRegionCode =
                provinceRegionCodeRepository.findById(cityRegionCode.getProvinceRegionCodeId())
                        .orElse(null);
        if (provinceRegionCode == null) return new MidBatchResult(Map.of(), null);

        // MidTemperature: 오전/오후 모두 morning(09:00) 기준으로 조회하므로 중복 제거
        List<LocalDateTime> morningTimes = missingTimes.stream()
                .map(et -> et.withHour(9))
                .distinct()
                .toList();
        Map<LocalDateTime, MidTemperature> midTempMap =
                midTemperatureRepository.findRecentAll(cityRegionCode, morningTimes);

        // MidLand: 실제 effectiveTime(09:00, 21:00) 기준으로 조회
        Map<LocalDateTime, MidLand> midLandMap =
                midLandRepository.findRecentAll(provinceRegionCode, missingTimes);

        // announceTime 추출: 첫 번째 MidLand 기준
        LocalDateTime midAnnounceTime = midLandMap.values().stream()
                .findFirst()
                .map(ml -> ml.getAnnounceTime().getTime())
                .orElse(null);

        Map<LocalDateTime, DailyRawItem> items = new HashMap<>();
        for (LocalDateTime et : missingTimes) {
            MidTemperature midTemp = midTempMap.get(et.withHour(9));
            MidLand midLand = midLandMap.get(et);

            if (midTemp == null || midLand == null) continue;

            Integer temp = isMorning(et) ? midTemp.getMinTemp() : midTemp.getMaxTemp();

            items.put(et, new DailyRawItem(et, temp, midLand.getPop()));
        }
        return new MidBatchResult(items, midAnnounceTime);
    }

    // ── 시간 계산 ───────────────────────────────────────────────────────

    private List<LocalDateTime> getEffectiveTimes(LocalDateTime now) {
        // 00시 ~ 05시 59분 사이라면 "전날" 데이터 기준으로 계산하기 위해 기준일을 하루 뺌
        LocalDateTime baseDate = now;
        if (now.getHour() < 6) {
            baseDate = now.minusDays(1);
        }

        LocalDateTime standardTime = baseDate
                .withHour(9).withMinute(0).withSecond(0).withNano(0);

        List<LocalDateTime> effectiveTimes = new ArrayList<>();
        for (int day = 0; day <= 6; day++) {
            effectiveTimes.add(standardTime.plusDays(day).withHour(9));
            effectiveTimes.add(standardTime.plusDays(day).withHour(21));
        }
        return effectiveTimes;
    }

    private boolean isMorning(LocalDateTime effectiveTime) {
        return effectiveTime.getHour() == 9;
    }

    // ── 내부 전용 타입 ──────────────────────────────────────────────────

    /**
     * composeMidBatch의 반환 타입. DailyRawItem 맵과 MidLand의 announceTime을 함께 전달.
     */
    private record MidBatchResult(
            Map<LocalDateTime, DailyRawItem> items,
            LocalDateTime announceTime
    ) {}

    /**
     * Composer 내부 전용 중간 타입
     */
    private record DailyRawItem(
            LocalDateTime effectiveTime,
            Integer temp,
            Integer pop
    ) {}
}