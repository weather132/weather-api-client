package com.github.yun531.climate.snapshot.daily.domain;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandRepository;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureRepository;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCodeRepository;
import com.github.yun531.climate.shortLand.domain.ShortLand;
import com.github.yun531.climate.shortLand.domain.ShortLandRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SnapshotService {
    private final ShortLandRepository shortLandRepository;
    private final MidLandRepository midLandRepository;
    private final MidTemperatureRepository midTemperatureRepository;
    private final ProvinceRegionCodeRepository provinceRegionCodeRepository;

    public SnapshotService(ShortLandRepository shortLandRepository, MidLandRepository midLandRepository, MidTemperatureRepository midTemperatureRepository, ProvinceRegionCodeRepository provinceRegionCodeRepository) {
        this.shortLandRepository = shortLandRepository;
        this.midLandRepository = midLandRepository;
        this.midTemperatureRepository = midTemperatureRepository;
        this.provinceRegionCodeRepository = provinceRegionCodeRepository;
    }

    public List<DailyForecastItem> getDailyForecastItems(CityRegionCode regionCode) {
        List<LocalDateTime> effectiveTimes = getEffectiveTimes(LocalDateTime.now());

        return effectiveTimes.stream()
                .map(efTime -> findDailyForecastItem(regionCode, efTime))
                .toList();
    }


    private List<LocalDateTime> getEffectiveTimes(LocalDateTime now) {
        LocalDateTime standardTime = now.withHour(9).withMinute(0).withSecond(0).withNano(0);

        List<LocalDateTime> effectiveTimes = new ArrayList<>();

        for (int day = 1; day <= 7; day++) {
            effectiveTimes.add(standardTime.plusDays(day).withHour(9));
            effectiveTimes.add(standardTime.plusDays(day).withHour(21));
        }

        return effectiveTimes;
    }

    private DailyForecastItem findDailyForecastItem(CityRegionCode regionCode, LocalDateTime effectiveTime) {
        ShortLand shortLand = shortLandRepository.findRecent(regionCode, effectiveTime);

        if (shortLand == null) {
            return findDailyForecastItemFromMid(regionCode, effectiveTime);
        }

        return new DailyForecastItem(shortLand);
    }

    private DailyForecastItem findDailyForecastItemFromMid(CityRegionCode regionCode, LocalDateTime effectiveTime) {
        MidTemperature midTemp = midTemperatureRepository.findRecent(regionCode, effectiveTime);
        Integer temp = isMorning(effectiveTime) ? midTemp.getMaxTemp() : midTemp.getMinTemp();

        ProvinceRegionCode provinceRegionCode = provinceRegionCodeRepository.findById(regionCode.getProvinceRegionCodeId()).get();
        MidLand midLand = midLandRepository.findRecent(provinceRegionCode, effectiveTime);

        return new DailyForecastItem(midLand.getAnnounceTime().formatIso(), effectiveTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), temp, midLand.getPop());
    }

    private boolean isMorning(LocalDateTime effectiveTime) {
        return effectiveTime.getHour() == 9;
    }
}
