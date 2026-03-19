package com.github.yun531.climate.snapshot.daily.application;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.snapshot.daily.domain.DailyForecastItem;
import com.github.yun531.climate.snapshot.daily.domain.SnapshotService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyService {
    private final CityRegionCodeRepository cityRegionCodeRepository;
    private final SnapshotService snapshotService;

    public DailyService(CityRegionCodeRepository cityRegionCodeRepository, SnapshotService snapshotService) {
        this.cityRegionCodeRepository = cityRegionCodeRepository;
        this.snapshotService = snapshotService;
    }

    public DailyForecast getDailyForecast(String regionCode) {
        CityRegionCode cityRegionCode = cityRegionCodeRepository.findByRegionCode(regionCode);

        List<DailyForecastItem> items = snapshotService.getDailyForecastItems(cityRegionCode);

        return new DailyForecast(regionCode, items);
    }
}
