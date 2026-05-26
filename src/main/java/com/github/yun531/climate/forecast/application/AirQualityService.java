package com.github.yun531.climate.forecast.application;

import com.github.yun531.climate.forecast.domain.reader.AirQualityViewReader;
import com.github.yun531.climate.forecast.domain.readmodel.AirQualityView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AirQualityService {

    private final AirQualityViewReader viewReader;

    public AirQualityView getAirQuality(String regionId) {
        return viewReader.loadAirQuality(regionId);
    }
}