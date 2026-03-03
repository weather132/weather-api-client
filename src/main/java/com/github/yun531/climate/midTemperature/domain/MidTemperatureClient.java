package com.github.yun531.climate.midTemperature.domain;

import com.github.yun531.climate.cityRegionCode.reference.CityRegionCode;
import com.github.yun531.climate.common.MidAnnounceTime;

import java.util.List;

public interface MidTemperatureClient {
    List<MidTemperature> requestMidTemperatures(MidAnnounceTime announceTime, List<CityRegionCode> regionCodes);
}