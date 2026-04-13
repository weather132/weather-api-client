package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.midLand.domain.MidLand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaMidLandRepository extends JpaRepository<MidLand, Long> {

    @Query(value = """
            SELECT mp.* FROM mid_pop mp
            INNER JOIN (
                SELECT effective_time, MAX(announce_time) AS max_at
                FROM mid_pop
                WHERE province_region_code_id = :regionId
                  AND effective_time IN (:effectiveTimes)
                GROUP BY effective_time
            ) latest ON mp.effective_time = latest.effective_time
                   AND mp.announce_time = latest.max_at
                   AND mp.province_region_code_id = :regionId
            """, nativeQuery = true)
    List<MidLand> findRecentByRegionAndEffectiveTimes(
            @Param("regionId") Long regionId,
            @Param("effectiveTimes") List<LocalDateTime> effectiveTimes
    );
}