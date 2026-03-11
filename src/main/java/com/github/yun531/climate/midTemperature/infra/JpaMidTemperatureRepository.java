package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMidTemperatureRepository extends JpaRepository<MidTemperature, Long> {
}
