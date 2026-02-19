package com.github.yun531.climate.shortLand.domain;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;

import java.util.List;

public interface ShortLandClient {
    List<ShortLand> requestShortLand(CityRegionCode cityRegionCode);
}
