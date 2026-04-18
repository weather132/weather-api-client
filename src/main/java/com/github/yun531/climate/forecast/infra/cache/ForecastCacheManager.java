package com.github.yun531.climate.forecast.infra.cache;

import com.github.yun531.climate.forecast.domain.readmodel.ForecastDailyView;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyView;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * forecast BC 전용 인메모리 캐시.
 */
@Component
public class ForecastCacheManager {

    private final ConcurrentHashMap<String, ForecastHourlyView> hourlyCache
            = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ForecastDailyView> dailyCache
            = new ConcurrentHashMap<>();

    @Nullable
    public ForecastHourlyView getHourly(String regionId) {
        return hourlyCache.get(regionId);
    }

    public void putHourly(String regionId, ForecastHourlyView view) {
        hourlyCache.put(regionId, view);
    }

    @Nullable
    public ForecastDailyView getDaily(String regionId) {
        return dailyCache.get(regionId);
    }

    public void putDaily(String regionId, ForecastDailyView view) {
        dailyCache.put(regionId, view);
    }

    public void invalidateHourly() {
        hourlyCache.clear();
    }

    public void invalidateDaily() {
        dailyCache.clear();
    }
}