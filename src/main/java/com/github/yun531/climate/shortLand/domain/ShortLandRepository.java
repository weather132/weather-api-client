package com.github.yun531.climate.shortLand.domain;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;

import java.time.LocalDateTime;
import java.util.List;

public interface ShortLandRepository {
    ShortLand save(ShortLand shortLand);
    void saveAll(List<ShortLand> shortLands);
    ShortLand findRecent(CityRegionCode regionCode, LocalDateTime effectiveTime);
}
