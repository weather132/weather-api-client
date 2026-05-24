package com.github.yun531.climate.forecast.domain.reader;

import com.github.yun531.climate.forecast.domain.readmodel.AirQualityView;

public interface AirQualityViewReader {
    AirQualityView loadAirQuality(String regionId);
}