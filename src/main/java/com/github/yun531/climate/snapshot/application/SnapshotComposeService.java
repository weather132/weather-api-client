package com.github.yun531.climate.snapshot.application;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.snapshot.contract.WeatherSnapshot;
import com.github.yun531.climate.snapshot.domain.SnapshotAssembler;
import com.github.yun531.climate.snapshot.domain.model.DailyForecastItem;
import com.github.yun531.climate.snapshot.domain.model.HourlyForecastItem;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * regionId 해석 -> Composer 호출 -> Assembler 조립.
 */
@Component
public class SnapshotComposeService {

    private final CityRegionCodeRepository cityRegionCodeRepository;
    private final HourlyForecastComposer hourlyForecastComposer;
    private final DailyForecastComposer dailyForecastComposer;
    private final SnapshotAssembler assembler;

    public SnapshotComposeService(
            CityRegionCodeRepository cityRegionCodeRepository,
            HourlyForecastComposer hourlyForecastComposer,
            DailyForecastComposer dailyForecastComposer,
            SnapshotAssembler assembler
    ) {
        this.cityRegionCodeRepository = cityRegionCodeRepository;
        this.hourlyForecastComposer = hourlyForecastComposer;
        this.dailyForecastComposer = dailyForecastComposer;
        this.assembler = assembler;
    }

    @Nullable
    public WeatherSnapshot composeSnapshot(String regionId, LocalDateTime announceTime) {
        CityRegionCode regionCode = cityRegionCodeRepository.findByRegionCode(regionId);
        if (regionCode == null) return null;

        List<HourlyForecastItem> hourlyItems = hourlyForecastComposer.compose(regionCode, announceTime);
        if (hourlyItems == null || hourlyItems.isEmpty()) {
            return null;
        }

        List<DailyForecastItem> dailyItems = dailyForecastComposer.compose(regionCode);
        if (dailyItems == null || dailyItems.isEmpty()) {
            return null;
        }

        return assembler.assemble(regionId, announceTime, hourlyItems, dailyItems);
    }
}