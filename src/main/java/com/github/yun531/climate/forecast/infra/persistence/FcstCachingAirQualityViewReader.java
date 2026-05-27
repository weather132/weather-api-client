package com.github.yun531.climate.forecast.infra.persistence;

import com.github.yun531.climate.forecast.domain.compose.FcstAirQualityComposer;
import com.github.yun531.climate.forecast.domain.reader.AirQualityViewReader;
import com.github.yun531.climate.forecast.domain.readmodel.AirQualityView;
import com.github.yun531.climate.forecast.domain.readmodel.RegionAirQualityView;
import com.github.yun531.climate.forecast.infra.cache.AirQualityCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FcstCachingAirQualityViewReader implements AirQualityViewReader {

    private final AirQualityCacheManager cacheManager;
    private final FcstAirQualityComposer composer;

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

    @Override
    public List<RegionAirQualityView> loadAirQualities(List<String> regionIds) {
        if (regionIds == null || regionIds.isEmpty()) return List.of();

        return loadEach(regionIds);
    }

    private List<RegionAirQualityView> loadEach(List<String> regionIds) {
        List<RegionAirQualityView> results = new ArrayList<>();
        for (String regionId : regionIds) {
            AirQualityView view = loadAirQuality(regionId);
            if (view != null) {
                results.add(new RegionAirQualityView(regionId, view));
            }
        }
        return results;
    }

    private boolean isBlank(String regionId) {
        return regionId == null || regionId.isBlank();
    }
}