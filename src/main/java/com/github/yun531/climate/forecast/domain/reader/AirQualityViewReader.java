package com.github.yun531.climate.forecast.domain.reader;

import com.github.yun531.climate.forecast.domain.readmodel.AirQualityView;
import com.github.yun531.climate.forecast.domain.readmodel.RegionAirQualityView;

import java.util.List;

public interface AirQualityViewReader {
    AirQualityView loadAirQuality(String regionId);

    List<RegionAirQualityView> loadAirQualities(List<String> regionIds);
}