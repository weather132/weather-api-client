package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.readmodel.PopView;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * notification BC 전용 POP 인메모리 캐시.
 */
@Component
public class PopCacheManager {

    private final ConcurrentHashMap<String, PopView> currentCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PopView> previousCache = new ConcurrentHashMap<>();

    public void rotate() {
        previousCache.clear();
        previousCache.putAll(currentCache);
        currentCache.clear();
    }

    @Nullable
    public PopView getCurrent(String regionId) {
        return currentCache.get(regionId);
    }

    @Nullable
    public PopView getPrevious(String regionId) {
        return previousCache.get(regionId);
    }

    public void putCurrent(String regionId, PopView view) {
        currentCache.put(regionId, view);
    }
}