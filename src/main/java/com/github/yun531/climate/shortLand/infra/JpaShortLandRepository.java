package com.github.yun531.climate.shortLand.infra;

import com.github.yun531.climate.shortLand.domain.ShortLand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaShortLandRepository extends JpaRepository<ShortLand, Long> {

    @Query(value = """
            SELECT sl.* FROM short_land sl
            INNER JOIN (
                SELECT effective_time, MAX(announce_time) AS max_at
                FROM short_land
                WHERE city_region_code_id = :regionId
                  AND effective_time IN (:effectiveTimes)
                GROUP BY effective_time
            ) latest ON sl.effective_time = latest.effective_time
                   AND sl.announce_time = latest.max_at
                   AND sl.city_region_code_id = :regionId
            """, nativeQuery = true)
    List<ShortLand> findRecentByRegionAndEffectiveTimes(
            @Param("regionId") Long regionId,
            @Param("effectiveTimes") List<LocalDateTime> effectiveTimes
    );
}