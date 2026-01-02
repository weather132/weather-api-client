package com.github.yun531.climate.repository;

import com.github.yun531.climate.entity.CityRegionCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRegionCodeRepository extends JpaRepository<CityRegionCode, Long> {
    CityRegionCode findByRegionCode(String regionCode);
}
