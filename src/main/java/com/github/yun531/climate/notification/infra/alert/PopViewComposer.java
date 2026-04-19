package com.github.yun531.climate.notification.infra.alert;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * regionId → DB 조회 → PopView 직접 생산.
 */
@Component
@RequiredArgsConstructor
public class PopViewComposer {

    private static final Hourly.Pop EMPTY_HOURLY_POP = new Hourly.Pop(null, null);

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

        LocalDateTime announceTime = shortGrids.isEmpty()
                ? null
                : shortGrids.get(0).getAnnounceTime().getTime();

        List<ShortGrid> sorted = shortGrids.stream()
                .filter(sg -> sg.getEffectiveTime() != null)
                .sorted(Comparator.comparing(ShortGrid::getEffectiveTime))
                .limit(PopView.HOURLY_SIZE)
                .toList();

        List<Hourly.Pop> pops = new ArrayList<>(PopView.HOURLY_SIZE);
        for (int i = 0; i < PopView.HOURLY_SIZE; i++) {
            if (i < sorted.size()) {
                ShortGrid sg = sorted.get(i);
                pops.add(new Hourly.Pop(sg.getEffectiveTime(), sg.getPop()));
            } else {
                pops.add(EMPTY_HOURLY_POP);
            }
        }
        return new HourlyResult(new Hourly(pops), announceTime);
    }

    // ── Daily: ShortLand → MidLand fallback, 7일 am/pm POP ─────────────

    private Daily composeDaily(CityRegionCode cityRegionCode) {
        List<LocalDateTime> effectiveTimes = getEffectiveTimes(LocalDateTime.now(clock));

        // ShortLand 배치 조회
        Map<LocalDateTime, ShortLand> shortLandItems =
                shortLandRepository.findRecentAll(cityRegionCode, effectiveTimes);

        // ShortLand 없는 시간대 → MidLand fallback
        List<LocalDateTime> missingTimes = effectiveTimes.stream()
                .filter(et -> !shortLandItems.containsKey(et))
                .toList();

        MidPopResult midResult = missingTimes.isEmpty()
                ? new MidPopResult(Map.of(), null)
                : composeMidPops(cityRegionCode, missingTimes);

        // effectiveTime → POP 수집
        List<PopSlot> popSlots = new ArrayList<>(effectiveTimes.size());
        for (LocalDateTime et : effectiveTimes) {
            ShortLand shortLand = shortLandItems.get(et);
            if (shortLand != null) {
                Integer pop = shortLand.getPop();
                if (pop == null) {
                    pop = shortLandRepository.findRecentPop(cityRegionCode, et);
                }
                popSlots.add(new PopSlot(et, pop));
            } else {
                Integer midPop = midResult.popMap().get(et);
                if (midPop != null) {
                    popSlots.add(new PopSlot(et, midPop));
                }
            }
        }

        // baseDate: ShortLand announceTime 우선, 없으면 MidLand announceTime
        LocalDateTime shortLandAnnounceTime = shortLandItems.values().stream()
                .findFirst()
                .map(ShortLand::getAnnounceTime)
                .orElse(null);

        LocalDateTime dailyAnnounceTime = (shortLandAnnounceTime != null)
                ? shortLandAnnounceTime
                : midResult.announceTime();

        LocalDate baseDate = (dailyAnnounceTime != null)
                ? dailyAnnounceTime.toLocalDate()
                : LocalDateTime.now(clock).toLocalDate();

        return aggregateDaily(baseDate, popSlots);
    }

    /**
     * MidLand fallback: POP + announceTime 추출.
     */
    private MidPopResult composeMidPops(
            CityRegionCode cityRegionCode, List<LocalDateTime> missingTimes
    ) {
        ProvinceRegionCode provinceRegionCode =
                provinceRegionCodeRepository.findById(cityRegionCode.getProvinceRegionCodeId())
                        .orElse(null);
        if (provinceRegionCode == null) return new MidPopResult(Map.of(), null);

        Map<LocalDateTime, MidLand> midLandMap =
                midLandRepository.findRecentAll(provinceRegionCode, missingTimes);

        LocalDateTime midAnnounceTime = midLandMap.values().stream()
                .findFirst()
                .map(ml -> ml.getAnnounceTime().getTime())
                .orElse(null);

        Map<LocalDateTime, Integer> popMap = new HashMap<>();
        for (LocalDateTime et : missingTimes) {
            MidLand midLand = midLandMap.get(et);
            if (midLand != null) {
                popMap.put(et, midLand.getPop());
            }
        }
        return new MidPopResult(popMap, midAnnounceTime);
    }

    // ── 집계 ────────────────────────────────────────────────────────────

    private Daily aggregateDaily(LocalDate baseDate, List<PopSlot> popSlots) {
        Map<Integer, List<PopSlot>> grouped = new HashMap<>();
        for (PopSlot slot : popSlots) {
            if (slot.effectiveTime() == null) continue;
            int daysAhead = (int) ChronoUnit.DAYS.between(
                    baseDate, slot.effectiveTime().toLocalDate());
            if (daysAhead < 0 || daysAhead >= PopView.DAILY_SIZE) continue;
            grouped.computeIfAbsent(daysAhead, k -> new ArrayList<>()).add(slot);
        }

        List<Daily.Pop> dailyPops = new ArrayList<>(PopView.DAILY_SIZE);
        for (int daysAhead = 0; daysAhead < PopView.DAILY_SIZE; daysAhead++) {
            List<PopSlot> daySlots = grouped.getOrDefault(daysAhead, List.of());
            dailyPops.add(aggregateDayPop(daySlots));
        }
        return new Daily(dailyPops);
    }

    private Daily.Pop aggregateDayPop(List<PopSlot> slots) {
        Integer amPop = null, pmPop = null;

        for (PopSlot slot : slots) {
            Integer pop = slot.pop();
            if (pop == null) continue;

            if (slot.effectiveTime().getHour() < 12) {
                amPop = (amPop == null) ? pop : Math.max(amPop, pop);
            } else {
                pmPop = (pmPop == null) ? pop : Math.max(pmPop, pop);
            }
        }
        return new Daily.Pop(amPop, pmPop);
    }

    // ── 시간 계산 ───────────────────────────────────────────────────────

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

    // ── 내부 전용 타입 ──────────────────────────────────────────────────

    private record HourlyResult(Hourly hourly, LocalDateTime announceTime) {}

    private record MidPopResult(Map<LocalDateTime, Integer> popMap, LocalDateTime announceTime) {}

    private record PopSlot(LocalDateTime effectiveTime, Integer pop) {}
}