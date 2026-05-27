package com.github.yun531.climate.forecast.application;

import com.github.yun531.climate.forecast.domain.reader.AirQualityViewReader;
import com.github.yun531.climate.forecast.domain.readmodel.AirQualityView;
import com.github.yun531.climate.forecast.domain.readmodel.RegionAirQualityView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AirQualityService {

    private final AirQualityViewReader viewReader;

    public AirQualityView getAirQuality(String regionId) {
        return viewReader.loadAirQuality(regionId);
    }

    public List<RegionAirQualityView> getAirQualities(List<String> regionIds) {
        return viewReader.loadAirQualities(regionIds);
    }
}