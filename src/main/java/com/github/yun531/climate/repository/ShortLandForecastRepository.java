package com.github.yun531.climate.repository;

import com.github.yun531.climate.entity.ShortLandForecast;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortLandForecastRepository extends JpaRepository<ShortLandForecast, Long> {
}
