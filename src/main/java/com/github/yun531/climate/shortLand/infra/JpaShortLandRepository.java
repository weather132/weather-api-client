package com.github.yun531.climate.shortLand.infra;

import com.github.yun531.climate.shortLand.domain.ShortLand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaShortLandRepository extends JpaRepository<ShortLand, Long> {
    List<ShortLand> findByCityRegionCodeIdAndEffectiveTime(Long cityRegionCodeId, LocalDateTime effectiveTime);
}
