package com.github.yun531.climate.airQuality.infra.persistence;

import com.github.yun531.climate.airQuality.domain.AirQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface JpaAirQualityRepository extends JpaRepository<AirQuality, Long> {

    @Query("select max(a.announceTime) from AirQuality a")
    LocalDateTime findMaxAnnounceTime();
}