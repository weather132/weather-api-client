package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaMidTemperatureRepository extends JpaRepository<MidTemperature, Long> {
    List<MidTemperature> findByCityRegionCodeIdAndEffectiveTime(Long cityRegionCodeId, LocalDateTime effectiveTime);
}
