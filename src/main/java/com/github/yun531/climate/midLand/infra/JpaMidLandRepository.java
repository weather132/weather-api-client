package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.midLand.domain.MidLand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaMidLandRepository extends JpaRepository<MidLand, Long> {
    List<MidLand> findByProvinceRegionCodeIdAndEffectiveTime(Long id, LocalDateTime effectiveTime);
}
