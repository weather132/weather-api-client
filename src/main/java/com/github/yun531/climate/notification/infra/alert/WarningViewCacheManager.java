package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.readmodel.WarningView;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * notification BC 전용 WarningView 인메모리 캐시.
 */
@Component
public class WarningViewCacheManager {

    private final ConcurrentHashMap<String, List<WarningView>> cache = new ConcurrentHashMap<>();

    @Nullable
    public List<WarningView> getWarningViews(String regionId) {
        return cache.get(regionId);
    }

    public void putWarningViews(String regionId, List<WarningView> views) {
        cache.put(regionId, views);
    }

    public void invalidate() {
        cache.clear();
    }
}