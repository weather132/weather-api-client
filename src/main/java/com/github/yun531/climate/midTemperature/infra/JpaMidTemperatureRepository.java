package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaMidTemperatureRepository extends JpaRepository<MidTemperature, Long> {

    @Query(value = """
            SELECT mt.* FROM mid_temperature mt
            INNER JOIN (
                SELECT effective_time, MAX(announce_time) AS max_at
                FROM mid_temperature
                WHERE city_region_code_id = :regionId
                  AND effective_time IN (:effectiveTimes)
                GROUP BY effective_time
            ) latest ON mt.effective_time = latest.effective_time
                   AND mt.announce_time = latest.max_at
                   AND mt.city_region_code_id = :regionId
            """, nativeQuery = true)
    List<MidTemperature> findRecentByRegionAndEffectiveTimes(
            @Param("regionId") Long regionId,
            @Param("effectiveTimes") List<LocalDateTime> effectiveTimes
    );
}