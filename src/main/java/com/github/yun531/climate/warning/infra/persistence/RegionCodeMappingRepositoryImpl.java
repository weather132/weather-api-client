package com.github.yun531.climate.warning.infra.persistence;

import com.github.yun531.climate.warning.domain.model.RegionCodeMapping;
import com.github.yun531.climate.warning.domain.repository.RegionCodeMappingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RegionCodeMappingRepositoryImpl implements RegionCodeMappingRepository {

    private final JpaRegionCodeMappingRepository jpaRepository;

    public RegionCodeMappingRepositoryImpl(JpaRegionCodeMappingRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<String> findWarningRegionCodes(String regionCode) {
        return jpaRepository.findByRegionCode(regionCode).stream()
                .map(RegionCodeMapping::getWarningRegionCode)
                .toList();
    }
}