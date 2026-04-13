package com.github.yun531.climate.midTemperature.domain;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MidTemperatureRepository {
    void saveAll(List<MidTemperature> midTemps);
    List<MidTemperature> findAll();
    MidTemperature findRecent(CityRegionCode cityRegionCode, LocalDateTime effectiveTime);
    Map<LocalDateTime, MidTemperature> findRecentAll(CityRegionCode regionCode, List<LocalDateTime> effectiveTimes);
}