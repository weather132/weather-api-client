package com.github.yun531.climate.forecast.infra.cache;

import com.github.yun531.climate.forecast.domain.readmodel.FcstDailyView;
import com.github.yun531.climate.forecast.domain.readmodel.FcstHourlyView;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * forecast BC 전용 인메모리 캐시.
 */
@Component
public class FcstCacheManager {

    private final ConcurrentHashMap<String, FcstHourlyView> hourlyCache
            = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FcstDailyView> dailyCache
            = new ConcurrentHashMap<>();

    @Nullable
    public FcstHourlyView getHourly(String regionId) {
        return hourlyCache.get(regionId);
    }

    public void putHourly(String regionId, FcstHourlyView view) {
        hourlyCache.put(regionId, view);
    }

    @Nullable
    public FcstDailyView getDaily(String regionId) {
        return dailyCache.get(regionId);
    }

    public void putDaily(String regionId, FcstDailyView view) {
        dailyCache.put(regionId, view);
    }

    public void invalidateHourly() {
        hourlyCache.clear();
    }

    public void invalidateDaily() {
        dailyCache.clear();
    }
}