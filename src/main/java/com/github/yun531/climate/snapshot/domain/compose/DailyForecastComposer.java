package com.github.yun531.climate.snapshot.domain.compose;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandRepository;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureRepository;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCodeRepository;
import com.github.yun531.climate.shortLand.domain.ShortLand;
import com.github.yun531.climate.shortLand.domain.ShortLandRepository;
import com.github.yun531.climate.snapshot.domain.model.DailyForecastItem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DailyForecastComposer {

    private final ShortLandRepository shortLandRepository;
    private final MidLandRepository midLandRepository;
    private final MidTemperatureRepository midTemperatureRepository;
    private final ProvinceRegionCodeRepository provinceRegionCodeRepository;

    public DailyForecastComposer(
            ShortLandRepository shortLandRepository,
            MidLandRepository midLandRepository,
            MidTemperatureRepository midTemperatureRepository,
            ProvinceRegionCodeRepository provinceRegionCodeRepository
    ) {
        this.shortLandRepository = shortLandRepository;
        this.midLandRepository = midLandRepository;
        this.midTemperatureRepository = midTemperatureRepository;
        this.provinceRegionCodeRepository = provinceRegionCodeRepository;
    }

    public List<DailyForecastItem> compose(CityRegionCode regionCode) {
        List<LocalDateTime> effectiveTimes = getEffectiveTimes(LocalDateTime.now());

        // ShortLand 배치 조회 (1회 쿼리)
        Map<LocalDateTime, ShortLand> shortLandItems =
                shortLandRepository.findRecentAll(regionCode, effectiveTimes);

        // ShortLand 없는 시간대 분류
        List<LocalDateTime> missingTimes = effectiveTimes.stream()
                .filter(et -> !shortLandItems.containsKey(et))
                .toList();

        // Mid fallback 배치 조회 (최대 3회 쿼리)
        Map<LocalDateTime, DailyForecastItem> midItems =
                missingTimes.isEmpty()
                        ? Map.of()
                        : composeMidBatch(regionCode, missingTimes);

        // 순서 유지하며 조립
        List<DailyForecastItem> result = new ArrayList<>(effectiveTimes.size());
        for (LocalDateTime effectiveTime : effectiveTimes) {
            ShortLand shortLand = shortLandItems.get(effectiveTime);
            if (shortLand != null) {
                Integer pop  = shortLand.getPop();
                Integer temp = shortLand.getTemp();

                if (pop == null) {
                    pop = shortLandRepository.findRecentPop(regionCode, effectiveTime);
                }
                if (temp == null) {
                    temp = isMorning(effectiveTime)
                            ? shortLandRepository.findRecentMinTemp(regionCode, effectiveTime)
                            : shortLandRepository.findRecentMaxTemp(regionCode, effectiveTime);
                }

                result.add(new DailyForecastItem(shortLand.getAnnounceTime(), effectiveTime, temp, pop));
            } else {
                DailyForecastItem midItem = midItems.get(effectiveTime);
                if (midItem != null) result.add(midItem);
            }
        }
        return result;
    }

    /**
     * Mid fallback 배치: ProvinceRegionCode 1회 + MidTemperature 1회 + MidLand 1회
     */
    private Map<LocalDateTime, DailyForecastItem> composeMidBatch(
            CityRegionCode regionCode, List<LocalDateTime> missingTimes
    ) {
        ProvinceRegionCode provinceRegionCode =
                provinceRegionCodeRepository.findById(regionCode.getProvinceRegionCodeId())
                        .orElse(null);
        if (provinceRegionCode == null) return Map.of();

        // MidTemperature: 오전/오후 모두 morning(09:00) 기준으로 조회하므로 중복 제거
        List<LocalDateTime> morningTimes = missingTimes.stream()
                .map(et -> et.withHour(9))
                .distinct()
                .toList();
        Map<LocalDateTime, MidTemperature> midTempMap =
                midTemperatureRepository.findRecentAll(regionCode, morningTimes);

        // MidLand: 실제 effectiveTime(09:00, 21:00) 기준으로 조회
        Map<LocalDateTime, MidLand> midLandMap =
                midLandRepository.findRecentAll(provinceRegionCode, missingTimes);

        Map<LocalDateTime, DailyForecastItem> result = new HashMap<>();
        for (LocalDateTime et : missingTimes) {
            MidTemperature midTemp = midTempMap.get(et.withHour(9));
            MidLand midLand = midLandMap.get(et);

            if (midTemp == null || midLand == null) continue;

            Integer temp = isMorning(et) ? midTemp.getMinTemp() : midTemp.getMaxTemp();

            result.put(et, new DailyForecastItem(
                    midLand.getAnnounceTime().getTime(), et, temp, midLand.getPop()));
        }
        return result;
    }

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
}