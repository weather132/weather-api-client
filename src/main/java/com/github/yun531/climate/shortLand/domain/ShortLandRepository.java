package com.github.yun531.climate.shortLand.domain;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ShortLandRepository {
    ShortLand save(ShortLand shortLand);
    void saveAll(List<ShortLand> shortLands);
    Map<LocalDateTime, ShortLand> findRecentAll(CityRegionCode regionCode, List<LocalDateTime> effectiveTimes);
    Integer findRecentPop(CityRegionCode regionCode, LocalDateTime effectiveTime);
    Integer findRecentMaxTemp(CityRegionCode regionCode, LocalDateTime effectiveTime);
    Integer findRecentMinTemp(CityRegionCode regionCode, LocalDateTime effectiveTime);
}