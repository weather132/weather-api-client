package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.readmodel.AirQualityView;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * notification BC 전용 AirQualityView 인메모리 캐시.
 */
@Component
public class AirQualityViewCacheManager {

    private final ConcurrentHashMap<String, AirQualityView> cache = new ConcurrentHashMap<>();

    @Nullable
    public AirQualityView getAirQualityView(String regionId) {
        return cache.get(regionId);
    }

    public void putAirQualityView(String regionId, AirQualityView view) {
        cache.put(regionId, view);
    }

    public void invalidate() {
        cache.clear();
    }
}