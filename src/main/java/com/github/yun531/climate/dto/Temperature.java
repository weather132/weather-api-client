package com.github.yun531.climate.dto;

import com.github.yun531.climate.entity.CityRegionCode;
import com.github.yun531.climate.entity.MidTemperature;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Temperature {
    private final String regionCode;
    private final LocalDateTime announceTime;
    private final LocalDateTime effectiveTime;
    private final Integer maxTemperature;
    private final Integer minTemperature;

    public MidTemperature toMidTemperatureEntity(CityRegionCode regionCode) {
        return new MidTemperature(announceTime, effectiveTime, regionCode, maxTemperature, minTemperature);
    }
}
