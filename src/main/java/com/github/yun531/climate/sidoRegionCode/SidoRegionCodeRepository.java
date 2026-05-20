package com.github.yun531.climate.sidoRegionCode;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SidoRegionCodeRepository extends JpaRepository<SidoRegionCode, Long> {
    Optional<SidoRegionCode> findByCode(String code);
}