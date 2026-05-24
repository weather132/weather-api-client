package com.github.yun531.climate.forecast.infra.persistence;

import com.github.yun531.climate.airQuality.domain.AirQuality;
import com.github.yun531.climate.airQuality.infra.persistence.JpaAirQualityRepository;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.forecast.domain.reader.AirQualityViewReader;
import com.github.yun531.climate.forecast.domain.readmodel.AirQualityGradeThresholds;
import com.github.yun531.climate.forecast.domain.readmodel.AirQualityView;
import com.github.yun531.climate.forecast.infra.cache.AirQualityCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CachingAirQualityViewReader implements AirQualityViewReader {

    private static final int FALLBACK_HOURS = 3;

    private final AirQualityCacheManager cacheManager;
    private final CityRegionCodeRepository cityRegionCodeRepository;
    private final JpaAirQualityRepository jpaAirQualityRepository;
    private final AirQualityGradeThresholds thresholds;
    private final Clock clock;

    @Override
    @Nullable
    public AirQualityView loadAirQuality(String regionId) {
        if (isBlank(regionId)) return null;

        AirQualityView cached = cacheManager.getAirQualityView(regionId);
        if (cached != null) return cached;

        AirQualityView view = composeView(regionId);
        cacheManager.putAirQualityView(regionId, view);
        return view;
    }

    private AirQualityView composeView(String regionId) {
        Long sidoId = resolveSidoId(regionId);
        if (sidoId == null) return emptyView();

        return toViewOrEmpty(findRecentWithinFallback(sidoId));
    }

    private Long resolveSidoId(String regionId) {
        CityRegionCode cityRegionCode = cityRegionCodeRepository.findByRegionCode(regionId);
        if (cityRegionCode == null) return null;
        return cityRegionCode.getSidoRegionCodeId();
    }

    private Optional<AirQuality> findRecentWithinFallback(Long sidoId) {
        LocalDateTime now = LocalDateTime.now(clock);
        return jpaAirQualityRepository.findRecentBySido(
                sidoId, now.minusHours(FALLBACK_HOURS), now, Limit.of(1));
    }

    private AirQualityView toViewOrEmpty(Optional<AirQuality> measurement) {
        return measurement.map(this::toView).orElseGet(this::emptyView);
    }

    private AirQualityView toView(AirQuality measurement) {
        return new AirQualityView(
                measurement.getAnnounceTime(),
                measurement.getPm10(), gradeOfPm10(measurement.getPm10()),
                measurement.getPm25(), gradeOfPm25(measurement.getPm25()));
    }

    @Nullable
    private String gradeOfPm10(@Nullable Integer value) {
        return value == null ? null : thresholds.classifyPm10(value);
    }

    @Nullable
    private String gradeOfPm25(@Nullable Integer value) {
        return value == null ? null : thresholds.classifyPm25(value);
    }

    private AirQualityView emptyView() {
        return new AirQualityView(null, null, null, null, null);
    }

    private boolean isBlank(String regionId) {
        return regionId == null || regionId.isBlank();
    }
}