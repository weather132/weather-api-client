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
import java.util.List;

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

    public List<DailyForecastItem> compose(
            CityRegionCode regionCode
    ) {
        List<LocalDateTime> effectiveTimes = getEffectiveTimes(LocalDateTime.now());

        return effectiveTimes.stream()
                .map(efTime -> composeDailyForecastItem(regionCode, efTime))
                .toList();
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

    private DailyForecastItem composeDailyForecastItem(
            CityRegionCode regionCode, LocalDateTime effectiveTime
    ) {
        ShortLand shortLand = shortLandRepository
                .findRecent(regionCode, effectiveTime);

        if (shortLand != null) {
            return DailyForecastItem.from(shortLand);
        }

        return composeDailyForecastItemFromMid(regionCode, effectiveTime);
    }

    private DailyForecastItem composeDailyForecastItemFromMid(
            CityRegionCode regionCode, LocalDateTime effectiveTime
    ) {
        MidTemperature midTemp = midTemperatureRepository
                .findRecent(regionCode, effectiveTime);
        Integer temp = isMorning(effectiveTime)
                ? midTemp.getMinTemp() : midTemp.getMaxTemp();

        ProvinceRegionCode provinceRegionCode = provinceRegionCodeRepository
                .findById(regionCode.getProvinceRegionCodeId()).get();
        MidLand midLand = midLandRepository
                .findRecent(provinceRegionCode, effectiveTime);

        return new DailyForecastItem(
                midLand.getAnnounceTime().getTime(),
                effectiveTime,
                temp,
                midLand.getPop()
        );
    }

    private boolean isMorning(LocalDateTime effectiveTime) {
        return effectiveTime.getHour() == 9;
    }
}