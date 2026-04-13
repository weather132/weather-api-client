package com.github.yun531.climate.shortLand.infra;

import com.github.yun531.climate.shortLand.domain.ShortLand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JpaShortLandRepository extends JpaRepository<ShortLand, Long> {

    @Query(value = """
            SELECT * FROM short_land
            WHERE city_region_code_id = :regionId
              AND effective_time = :effectiveTime
            ORDER BY announce_time DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<ShortLand> findRecentByRegionAndEffectiveTime(
            @Param("regionId") Long regionId,
            @Param("effectiveTime") LocalDateTime effectiveTime
    );
}