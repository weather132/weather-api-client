package com.github.yun531.climate.midTemperature.domain;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;

import java.time.LocalDateTime;
import java.util.List;

public interface MidTemperatureRepository {
    void saveAll(List<MidTemperature> midTemps);
    List<MidTemperature> findAll();
    MidTemperature findRecent(CityRegionCode cityRegionCode, LocalDateTime effectiveTime);
}