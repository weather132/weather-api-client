package com.github.yun531.climate.forecast.infra.persistence;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.forecast.domain.compose.DailyForecastComposer;
import com.github.yun531.climate.forecast.domain.compose.DailyForecastComposer.DailyComposeResult;
import com.github.yun531.climate.forecast.domain.compose.HourlyForecastComposer;
import com.github.yun531.climate.forecast.domain.compose.HourlyForecastComposer.HourlyComposeResult;
import com.github.yun531.climate.forecast.domain.reader.ForecastViewReader;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastDailyView;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyView;
import com.github.yun531.climate.forecast.infra.cache.ForecastCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * {@link ForecastViewReader} 구현체.
 *
 * <p>캐시를 앞에 두고 미스 시 Composer를 호출하는 구조.
 * Mapper 불필요: Composer가 readmodel을 직접 생산하므로 변환할 대상이 없음.
 */
@Component
@RequiredArgsConstructor
public class CachingForecastViewReader implements ForecastViewReader {

    private final ForecastCacheManager cache;
    private final HourlyForecastComposer hourlyComposer;
    private final DailyForecastComposer dailyComposer;
    private final CityRegionCodeRepository cityRegionCodeRepository;

    @Override
    @Nullable
    public ForecastHourlyView loadHourly(String regionId) {
        ForecastHourlyView cached = cache.getHourly(regionId);
        if (cached != null) return cached;

        CityRegionCode cityRegionCode = cityRegionCodeRepository.findByRegionCode(regionId);
        if (cityRegionCode == null) return null;

        HourlyComposeResult result = hourlyComposer.compose(cityRegionCode);
        if (result.forecastHourlyPoints().isEmpty()) return null;

        ForecastHourlyView view = new ForecastHourlyView(
                regionId, result.announceTime(), result.forecastHourlyPoints());
        cache.putHourly(regionId, view);
        return view;
    }

    @Override
    @Nullable
    public ForecastDailyView loadDaily(String regionId) {
        ForecastDailyView cached = cache.getDaily(regionId);
        if (cached != null) return cached;

        CityRegionCode cityRegionCode = cityRegionCodeRepository.findByRegionCode(regionId);
        if (cityRegionCode == null) return null;

        DailyComposeResult result = dailyComposer.compose(cityRegionCode);
        if (result.forecastDailyPoints().isEmpty()) return null;

        ForecastDailyView view = new ForecastDailyView(
                regionId, result.announceTime(), result.forecastDailyPoints());
        cache.putDaily(regionId, view);
        return view;
    }
}