package com.github.yun531.climate.forecast.domain.compose;

import com.github.yun531.climate.airQuality.domain.AirQuality;
import com.github.yun531.climate.airQuality.domain.AirQualityRepository;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.forecast.domain.readmodel.AirQualityGradeThresholds;
import com.github.yun531.climate.forecast.domain.readmodel.AirQualityView;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * regionId → 시도 식별 → DB 조회 → 등급 평가 → AirQualityView 생산.
 */
@Component
@RequiredArgsConstructor
public class FcstAirQualityComposer {

    private final CityRegionCodeRepository cityRegionCodeRepository;
    private final AirQualityRepository airQualityRepository;
    private final AirQualityGradeThresholds thresholds;

    public AirQualityView compose(String regionId) {
        Long sidoId = resolveSidoId(regionId);
        if (sidoId == null) return emptyView();

        return toViewOrEmpty(findLatestAirQuality(sidoId));
    }

    private Long resolveSidoId(String regionId) {
        CityRegionCode cityRegionCode = cityRegionCodeRepository.findByRegionCode(regionId);
        if (cityRegionCode == null) return null;
        return cityRegionCode.getSidoRegionCodeId();
    }

    private Optional<AirQuality> findLatestAirQuality(Long sidoId) {
        return airQualityRepository.findLatestBySido(sidoId);
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
}