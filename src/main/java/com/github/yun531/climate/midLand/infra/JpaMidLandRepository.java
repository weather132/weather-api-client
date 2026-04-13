package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.midLand.domain.MidLand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JpaMidLandRepository extends JpaRepository<MidLand, Long> {

    @Query(value = """
            SELECT * FROM mid_pop
            WHERE province_region_code_id = :regionId
              AND effective_time = :effectiveTime
            ORDER BY announce_time DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<MidLand> findRecentByRegionAndEffectiveTime(
            @Param("regionId") Long regionId,
            @Param("effectiveTime") LocalDateTime effectiveTime
    );
}