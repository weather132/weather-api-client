package com.github.yun531.climate.midLand.domain;

import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MidLandRepository {
    void saveAll(List<MidLand> midLands);
    MidLand findById(Long id);
    List<MidLand> findAll();
    MidLand findRecent(ProvinceRegionCode regionCode, LocalDateTime effectiveTime);
    Map<LocalDateTime, MidLand> findRecentAll(ProvinceRegionCode regionCode, List<LocalDateTime> effectiveTimes);
}