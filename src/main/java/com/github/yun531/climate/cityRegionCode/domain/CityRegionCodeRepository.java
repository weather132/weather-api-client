package com.github.yun531.climate.cityRegionCode.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRegionCodeRepository extends JpaRepository<CityRegionCode, Long> {
    Long findByRegionCode(String regionCode);
}
