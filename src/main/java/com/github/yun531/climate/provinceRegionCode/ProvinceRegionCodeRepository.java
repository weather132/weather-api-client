package com.github.yun531.climate.provinceRegionCode;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvinceRegionCodeRepository extends JpaRepository<ProvinceRegionCode, Long> {
    Long findByRegionCode(String regionCode);
}