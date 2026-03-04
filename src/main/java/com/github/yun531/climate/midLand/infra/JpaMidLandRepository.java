package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.midLand.domain.MidLand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMidLandRepository extends JpaRepository<MidLand, Long> {
}
