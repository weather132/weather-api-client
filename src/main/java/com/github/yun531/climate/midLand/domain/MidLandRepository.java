package com.github.yun531.climate.midLand.domain;

import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;

import java.time.LocalDateTime;
import java.util.List;

public interface MidLandRepository {
    void saveAll(List<MidLand> midLands);
    MidLand findById(Long id);
    List<MidLand> findAll();
    MidLand findRecent(ProvinceRegionCode regionCode, LocalDateTime effectiveTime);
}
