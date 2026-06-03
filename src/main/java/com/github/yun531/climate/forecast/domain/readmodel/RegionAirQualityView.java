package com.github.yun531.climate.forecast.domain.readmodel;

/**
 * 다지역 미세먼지 조회 응답 단위.
 * regionId 와 해당 지역의 AirQualityView를 묶음.
 */
public record RegionAirQualityView(
        String regionId,
        AirQualityView view
) {}