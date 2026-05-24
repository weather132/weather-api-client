package com.github.yun531.climate.airQuality.infra.persistence;

import com.github.yun531.climate.airQuality.domain.AirQuality;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JpaAirQualityRepository extends JpaRepository<AirQuality, Long> {

    @Query("select max(a.announceTime) from AirQuality a")
    LocalDateTime findMaxAnnounceTime();

    /**
     * 특정 시도의 [from, to] 구간 내 가장 최신 측정 1건.
     */
    @Query("""
            select a from AirQuality a
            where a.sidoRegionCodeId = :sidoId
              and a.announceTime between :from and :to
            order by a.announceTime desc
            """)
    Optional<AirQuality> findRecentBySido(@Param("sidoId") Long sidoId,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          Limit limit);
}