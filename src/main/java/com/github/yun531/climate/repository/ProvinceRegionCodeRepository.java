package com.github.yun531.climate.repository;

import com.github.yun531.climate.entity.ProvinceRegionCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvinceRegionCodeRepository extends JpaRepository<ProvinceRegionCode, Long> {
    ProvinceRegionCode findByRegionCode(String regionCode);
}
