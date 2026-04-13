package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JpaMidTemperatureRepository extends JpaRepository<MidTemperature, Long> {

    @Query(value = """
            SELECT * FROM mid_temperature
            WHERE city_region_code_id = :regionId
              AND effective_time = :effectiveTime
            ORDER BY announce_time DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<MidTemperature> findRecentByRegionAndEffectiveTime(
            @Param("regionId") Long regionId,
            @Param("effectiveTime") LocalDateTime effectiveTime
    );
}