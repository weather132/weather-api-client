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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
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
    private final Clock clock;

    public record DailyComposeResult(
            LocalDateTime announceTime,
            List<ForecastDailyPoint> forecastDailyPoints
    ) {}

    public DailyComposeResult compose(CityRegionCode cityRegionCode) {
        List<LocalDateTime> effectiveTimes           = getEffectiveTimes(LocalDateTime.now(clock));
        Map<LocalDateTime, ShortLand> shortLandItems = fetchRecentShortLands(cityRegionCode, effectiveTimes);
        List<LocalDateTime> missingTimes             = findMissingTimes(effectiveTimes, shortLandItems);
        MidBatchResult midResult                     = composeMidBatch(cityRegionCode, missingTimes);

        Map<LocalDateTime, DailyRawItem> rawItemMap  =
                collectRawItems(effectiveTimes, shortLandItems, midResult.items(), cityRegionCode);
        LocalDateTime announceTime                   = extractAnnounceTime(shortLandItems, midResult);
        List<ForecastDailyPoint> forecastDailyPoints = buildDaily(effectiveTimes, rawItemMap);

        return new DailyComposeResult(announceTime, forecastDailyPoints);
    }


    private Map<LocalDateTime, ShortLand> fetchRecentShortLands(
            CityRegionCode cityRegionCode, List<LocalDateTime> effectiveTimes) {
        return shortLandRepository.findRecentAll(cityRegionCode, effectiveTimes);
    }

    private List<LocalDateTime> findMissingTimes(
            List<LocalDateTime> effectiveTimes,
            Map<LocalDateTime, ShortLand> shortLandItems) {
        return effectiveTimes.stream()
                .filter(et -> !shortLandItems.containsKey(et))
                .toList();
    }

    /**
     * effectiveTime별 DailyRawItem을 수집.
     * 우선순위: ShortLand → ShortLand(이전 발표) → MidLand/MidTemperature
     */
    private Map<LocalDateTime, DailyRawItem> collectRawItems(
            List<LocalDateTime> effectiveTimes,
            Map<LocalDateTime, ShortLand> shortLandItems,
            Map<LocalDateTime, DailyRawItem> midItems,
            CityRegionCode cityRegionCode
    ) {
        Map<LocalDateTime, DailyRawItem> rawItemMap = new HashMap<>();
        for (LocalDateTime et : effectiveTimes) {
            DailyRawItem item = findRawItemForTime(et, shortLandItems, midItems, cityRegionCode);
            if (item != null) {
                rawItemMap.put(et, item);
            }
        }
        return rawItemMap;
    }

    /**
     * 단일 effectiveTime에 대한 temp/pop을 찾는다.
     * ShortLand 존재 → getPop()/getTemp() 우선, null 이면 이전 발표 시각에서 조회.
     * ShortLand 없음 → MidLand/MidTemperature fallback.
     */
    @Nullable
    private DailyRawItem findRawItemForTime(
            LocalDateTime et,
            Map<LocalDateTime, ShortLand> shortLandItems,
            Map<LocalDateTime, DailyRawItem> midItems,
            CityRegionCode cityRegionCode
    ) {
        ShortLand shortLand = shortLandItems.get(et);
        if (shortLand != null) {
            Integer pop = shortLand.getPop();
            Integer temp = shortLand.getTemp();

            if (pop == null) {
                pop = shortLandRepository.findRecentPop(cityRegionCode, et);
            }
            if (temp == null) {
                temp = isMorning(et)
                        ? shortLandRepository.findRecentMinTemp(cityRegionCode, et)
                        : shortLandRepository.findRecentMaxTemp(cityRegionCode, et);
            }
            return new DailyRawItem(temp, pop);
        }
        return midItems.get(et);
    }

    private LocalDateTime extractAnnounceTime(
            Map<LocalDateTime, ShortLand> shortLandItems,
            MidBatchResult midResult
    ) {
        LocalDateTime shortLandAnnounceTime = shortLandItems.values().stream()
                .findFirst()
                .map(ShortLand::getAnnounceTime)
                .orElse(null);

        return (shortLandAnnounceTime != null)
                ? shortLandAnnounceTime
                : midResult.announceTime();
    }


    /**
     * effectiveTimes 인덱스 기반으로 ForecastDailyPoint를 조립.
     * effectiveTimes는 [D+0 09:00, D+0 21:00, D+1 09:00, D+1 21:00, ...] 고정 순서.
     * → index day*2 = AM(09:00, minTemp), day*2+1 = PM(21:00, maxTemp)
     * rawItemMap 비어있음 → 빈 리스트 반환.
     */
    private List<ForecastDailyPoint> buildDaily(
            List<LocalDateTime> effectiveTimes,
            Map<LocalDateTime, DailyRawItem> rawItemMap
    ) {
        if (rawItemMap.isEmpty()) return List.of();

        List<ForecastDailyPoint> dailyPoints = new ArrayList<>(DAILY_RANGE);
        for (int day = 0; day < DAILY_RANGE; day++) {
            DailyRawItem amItem = rawItemMap.get(effectiveTimes.get(day * 2));
            DailyRawItem pmItem = rawItemMap.get(effectiveTimes.get(day * 2 + 1));

            Integer minTemp = amItem != null ? amItem.temp() : null;
            Integer maxTemp = pmItem != null ? pmItem.temp() : null;
            Integer amPop   = amItem != null ? amItem.pop() : null;
            Integer pmPop   = pmItem != null ? pmItem.pop() : null;

            dailyPoints.add(new ForecastDailyPoint(day, minTemp, maxTemp, amPop, pmPop));
        }
        return dailyPoints;
    }

    // --- Mid (Fallback) 처리 로직 ---

    /**
     * Mid fallback 배치: ProvinceRegionCode 1회 + MidTemperature 1회 + MidLand 1회.
     * missingTimes 비어있음 → empty MidBatchResult 반환.
     */
    private MidBatchResult composeMidBatch(
            CityRegionCode cityRegionCode, List<LocalDateTime> missingTimes
    ) {
        if (missingTimes.isEmpty()) return new MidBatchResult(Map.of(), null);

        ProvinceRegionCode provinceRegionCode =
                provinceRegionCodeRepository.findById(cityRegionCode.getProvinceRegionCodeId())
                        .orElse(null);
        if (provinceRegionCode == null) return new MidBatchResult(Map.of(), null);

        Map<LocalDateTime, MidTemperature> midTempMap = findMidTemperatures(cityRegionCode, missingTimes);
        Map<LocalDateTime, MidLand> midLandMap        = findMidLands(provinceRegionCode, missingTimes);

        Map<LocalDateTime, DailyRawItem> items        = buildMidRawItems(missingTimes, midTempMap, midLandMap);
        LocalDateTime midAnnounceTime                 = extractMidAnnounceTime(midLandMap);

        return new MidBatchResult(items, midAnnounceTime);
    }

    /**
     * MidTemp 배치 조회: AM 시각(09:00) 기준으로 중복 제거 후 조회.
     * MidTemp는 AM/PM 구분 없이, 발효시간이 09시로 저장되어 있음.
     */
    private Map<LocalDateTime, MidTemperature> findMidTemperatures(
            CityRegionCode cityRegionCode, List<LocalDateTime> missingTimes) {
        List<LocalDateTime> morningTimes = missingTimes.stream()
                .map(et -> et.withHour(9))
                .distinct()
                .toList();
        return midTemperatureRepository.findRecentAll(cityRegionCode, morningTimes);
    }

    private Map<LocalDateTime, MidLand> findMidLands(
            ProvinceRegionCode provinceRegionCode, List<LocalDateTime> missingTimes) {
        return midLandRepository.findRecentAll(provinceRegionCode, missingTimes);
    }

    /**
     * missingTimes 각 시각에 대해 Mid 데이터로 DailyRawItem을 조립.
     * isMorning(et) → minTemp, 아니면 maxTemp 매핑.
     */
    private Map<LocalDateTime, DailyRawItem> buildMidRawItems(
            List<LocalDateTime> missingTimes,
            Map<LocalDateTime, MidTemperature> midTempMap,
            Map<LocalDateTime, MidLand> midLandMap
    ) {
        Map<LocalDateTime, DailyRawItem> items = new HashMap<>();
        for (LocalDateTime et : missingTimes) {
            MidTemperature midTemp = midTempMap.get(et.withHour(9));
            MidLand midLand        = midLandMap.get(et);

            if (midTemp == null || midLand == null) continue;

            Integer temp = isMorning(et) ? midTemp.getMinTemp() : midTemp.getMaxTemp();
            items.put(et, new DailyRawItem(temp, midLand.getPop()));
        }
        return items;
    }

    private LocalDateTime extractMidAnnounceTime(Map<LocalDateTime, MidLand> midLandMap) {
        return midLandMap.values().stream()
                .findFirst()
                .map(ml -> ml.getAnnounceTime().getTime())
                .orElse(null);
    }


    private List<LocalDateTime> getEffectiveTimes(LocalDateTime now) {
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


    private record MidBatchResult(
            Map<LocalDateTime, DailyRawItem> items,
            LocalDateTime announceTime
    ) {}

    private record DailyRawItem(Integer temp, Integer pop) {}
}