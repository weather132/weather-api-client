package com.github.yun531.climate.forecast.infra.persistence;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.forecast.domain.compose.DailyFcstComposer;
import com.github.yun531.climate.forecast.domain.compose.DailyFcstComposer.DailyComposeResult;
import com.github.yun531.climate.forecast.domain.compose.HourlyFcstComposer;
import com.github.yun531.climate.forecast.domain.compose.HourlyFcstComposer.HourlyComposeResult;
import com.github.yun531.climate.forecast.domain.reader.FcstViewReader;
import com.github.yun531.climate.forecast.domain.readmodel.FcstDailyView;
import com.github.yun531.climate.forecast.domain.readmodel.FcstHourlyView;
import com.github.yun531.climate.forecast.infra.cache.FcstCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CachingFcstViewReader implements FcstViewReader {

    private final FcstCacheManager cache;
    private final HourlyFcstComposer hourlyComposer;
    private final DailyFcstComposer dailyComposer;
    private final CityRegionCodeRepository cityRegionCodeRepository;

    @Override
    @Nullable
    public FcstHourlyView loadHourly(String regionId) {
        FcstHourlyView cached = cache.getHourly(regionId);
        if (cached != null) return cached;

        CityRegionCode cityRegionCode = cityRegionCodeRepository.findByRegionCode(regionId);
        if (cityRegionCode == null) return null;

        HourlyComposeResult result = hourlyComposer.compose(cityRegionCode);
        if (result.fcstHourlyPoints().isEmpty()) return null;

        FcstHourlyView view = new FcstHourlyView(
                regionId, result.announceTime(), result.fcstHourlyPoints());
        cache.putHourly(regionId, view);
        return view;
    }

    @Override
    @Nullable
    public FcstDailyView loadDaily(String regionId) {
        FcstDailyView cached = cache.getDaily(regionId);
        if (cached != null) return cached;

        CityRegionCode cityRegionCode = cityRegionCodeRepository.findByRegionCode(regionId);
        if (cityRegionCode == null) return null;

        DailyComposeResult result = dailyComposer.compose(cityRegionCode);
        if (result.fcstDailyPoints().isEmpty()) return null;

        FcstDailyView view = new FcstDailyView(
                regionId, result.announceTime(), result.fcstDailyPoints());
        cache.putDaily(regionId, view);
        return view;
    }
}