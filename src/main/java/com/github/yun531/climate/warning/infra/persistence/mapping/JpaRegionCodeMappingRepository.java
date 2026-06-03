package com.github.yun531.climate.warning.infra.persistence.mapping;

import com.github.yun531.climate.warning.domain.mapping.RegionCodeMapping;
import com.github.yun531.climate.warning.domain.mapping.RegionCodeMappingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaRegionCodeMappingRepository extends JpaRepository<RegionCodeMapping, RegionCodeMappingId> {
    List<RegionCodeMapping> findByRegionCode(String regionCode);
}