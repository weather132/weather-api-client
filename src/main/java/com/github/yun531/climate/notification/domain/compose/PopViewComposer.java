package com.github.yun531.climate.notification.domain.compose;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.cityRegionCode.domain.Coordinates;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandRepository;
import com.github.yun531.climate.notification.domain.readmodel.PopView;
import com.github.yun531.climate.notification.domain.readmodel.PopView.Daily;
import com.github.yun531.climate.notification.domain.readmodel.PopView.Hourly;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCodeRepository;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import com.github.yun531.climate.shortGrid.domain.ShortGridRepository;
import com.github.yun531.climate.shortLand.domain.ShortLand;
import com.github.yun531.climate.shortLand.domain.ShortLandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

/**
 * regionId → DB 조회 → PopView 직접 생산.
 */
@Component
@RequiredArgsConstructor
public class PopViewComposer {

    private final ShortGridRepository shortGridRepository;
    private final ShortLandRepository shortLandRepository;
    private final MidLandRepository midLandRepository;
    private final ProvinceRegionCodeRepository provinceRegionCodeRepository;
    private final CityRegionCodeRepository cityRegionCodeRepository;
    private final Clock clock;

    @Nullable
    public PopView compose(String regionId) {
        CityRegionCode cityRegionCode = cityRegionCodeRepository.findByRegionCode(regionId);
        if (cityRegionCode == null) return null;

        HourlyResult hourlyResult = composeHourly(cityRegionCode);
        Daily daily = composeDaily(cityRegionCode);

        return new PopView(hourlyResult.hourly(), daily, hourlyResult.announceTime());
    }

    // ── Hourly: ShortGrid → POP + effectiveTime, 26개 고정 ──────────────

    private HourlyResult composeHourly(CityRegionCode cityRegionCode) {
        Coordinates coords = cityRegionCode.getCoordinates();
        List<ShortGrid> shortGrids = shortGridRepository
                .findRecentByXAndY(coords.getX(), coords.getY());

        LocalDateTime announceTime = extractAnnounceTime(shortGrids);
        Hourly hourly = buildHourly(shortGrids);

        return new HourlyResult(hourly, announceTime);
    }

    private LocalDateTime extractAnnounceTime(List<ShortGrid> shortGrids) {
        return shortGrids.isEmpty()
                ? null
                : shortGrids.get(0).getAnnounceTime().getTime();
    }

    private Hourly buildHourly(List<ShortGrid> shortGrids) {
        List<Hourly.Pop> available = shortGrids.stream()
                .filter(sg -> sg.getEffectiveTime() != null)
                .sorted(Comparator.comparing(ShortGrid::getEffectiveTime))
                .limit(PopView.HOURLY_SIZE)
                .map(sg -> new Hourly.Pop(sg.getEffectiveTime(), sg.getPop()))
                .toList();

        return Hourly.padded(available);
    }

    // ── Daily: ShortLand → MidLand fallback, 7일 AM/PM POP ──────────────

    private Daily composeDaily(CityRegionCode cityRegionCode) {
        List<LocalDateTime> effectiveTimes = getEffectiveTimes(LocalDateTime.now(clock));

        Map<LocalDateTime, ShortLand> shortLandItems =
                shortLandRepository.findRecentAll(cityRegionCode, effectiveTimes);

        List<LocalDateTime> missingTimes = effectiveTimes.stream()
                .filter(et -> !shortLandItems.containsKey(et))
                .toList();

        Map<LocalDateTime, Integer> midPops = missingTimes.isEmpty()
                ? Map.of()
                : composeMidPops(cityRegionCode, missingTimes);

        Map<LocalDateTime, Integer> popMap =
                collectPops(effectiveTimes, shortLandItems, midPops, cityRegionCode);

        return buildDaily(effectiveTimes, popMap);
    }

    /**
     * effectiveTime별 POP 값을 수집한다.
     * 우선순위: ShortLand(최신 발표) → ShortLand(직전 발표 보충) → MidLand
     */
    private Map<LocalDateTime, Integer> collectPops(
            List<LocalDateTime> effectiveTimes,
            Map<LocalDateTime, ShortLand> shortLandItems,
            Map<LocalDateTime, Integer> midPops,
            CityRegionCode cityRegionCode
    ) {
        Map<LocalDateTime, Integer> popMap = new HashMap<>();
        for (LocalDateTime et : effectiveTimes) {
            Integer pop = findPopForTime(et, shortLandItems, midPops, cityRegionCode);
            if (pop != null) {
                popMap.put(et, pop);
            }
        }
        return popMap;
    }

    /**
     * 단일 effectiveTime에 대한 POP를 찾는다.
     * ShortLand 존재 시 최신 발표 값을 사용, 일부 데이터가 누락되었으면 직전 발표에서 보충.
     * ShortLand 자체가 없으면 MidLand fallback.
     */
    @Nullable
    private Integer findPopForTime(
            LocalDateTime et,
            Map<LocalDateTime, ShortLand> shortLandItems,
            Map<LocalDateTime, Integer> midPops,
            CityRegionCode cityRegionCode
    ) {
        ShortLand shortLand = shortLandItems.get(et);
        if (shortLand != null) {
            Integer pop = shortLand.getPop();
            return (pop != null) ? pop : shortLandRepository.findRecentPop(cityRegionCode, et);
        }
        return midPops.get(et);
    }

    /**
     * MidLand fallback: missingTimes에 대한 POP Map 반환.
     */
    private Map<LocalDateTime, Integer> composeMidPops(
            CityRegionCode cityRegionCode, List<LocalDateTime> missingTimes
    ) {
        ProvinceRegionCode provinceRegionCode =
                provinceRegionCodeRepository.findById(cityRegionCode.getProvinceRegionCodeId())
                        .orElse(null);
        if (provinceRegionCode == null) return Map.of();

        Map<LocalDateTime, MidLand> midLandMap =
                midLandRepository.findRecentAll(provinceRegionCode, missingTimes);

        Map<LocalDateTime, Integer> popMap = new HashMap<>();
        for (LocalDateTime et : missingTimes) {
            MidLand midLand = midLandMap.get(et);
            if (midLand != null) {
                popMap.put(et, midLand.getPop());
            }
        }
        return popMap;
    }

    /**
     * effectiveTimes 인덱스 기반으로 Daily를 조립한다.
     * effectiveTimes는 [D+0 09:00, D+0 21:00, D+1 09:00, D+1 21:00, ...] 고정 순서.
     * → index day*2 = AM(09:00), day*2+1 = PM(21:00)
     */
    private Daily buildDaily(List<LocalDateTime> effectiveTimes,
                             Map<LocalDateTime, Integer> popMap) {
        List<Daily.Pop> dailyPops = new ArrayList<>(PopView.DAILY_SIZE);
        for (int day = 0; day < PopView.DAILY_SIZE; day++) {
            Integer amPop = popMap.get(effectiveTimes.get(day * 2));
            Integer pmPop = popMap.get(effectiveTimes.get(day * 2 + 1));
            dailyPops.add(new Daily.Pop(amPop, pmPop));
        }
        return new Daily(dailyPops);
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

    private record HourlyResult(Hourly hourly, LocalDateTime announceTime) {}
}