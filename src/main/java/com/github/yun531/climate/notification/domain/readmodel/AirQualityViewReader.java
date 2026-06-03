package com.github.yun531.climate.notification.domain.readmodel;

public interface AirQualityViewReader {
    AirQualityView loadAirQuality(String regionId);
}