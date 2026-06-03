package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.compose.AirQualityComposer;
import com.github.yun531.climate.notification.domain.readmodel.AirQualityView;
import com.github.yun531.climate.notification.domain.readmodel.AirQualityViewReader;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CachingAirQualityViewReader implements AirQualityViewReader {

    private final AirQualityViewCacheManager cacheManager;
    private final AirQualityComposer composer;

    @Override
    @Nullable
    public AirQualityView loadAirQuality(String regionId) {
        if (isBlank(regionId)) return null;

        AirQualityView cached = cacheManager.getAirQualityView(regionId);
        if (cached != null) return cached;

        AirQualityView view = composer.compose(regionId);
        cacheManager.putAirQualityView(regionId, view);
        return view;
    }

    private boolean isBlank(String regionId) {
        return regionId == null || regionId.isBlank();
    }
}