package com.github.yun531.climate.warning.infra.persistence;

import com.github.yun531.climate.warning.domain.model.RegionCodeMapping;
import com.github.yun531.climate.warning.domain.model.RegionCodeMappingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaRegionCodeMappingRepository extends JpaRepository<RegionCodeMapping, RegionCodeMappingId> {
    List<RegionCodeMapping> findByRegionCode(String regionCode);
}