package com.github.yun531.climate.repository;

import com.github.yun531.climate.entity.ShortTemperature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortTemperatureRepository extends JpaRepository<ShortTemperature, Long> {
}
