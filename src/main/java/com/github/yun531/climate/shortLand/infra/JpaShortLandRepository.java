package com.github.yun531.climate.shortLand.infra;

import com.github.yun531.climate.shortLand.domain.ShortLand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaShortLandRepository extends JpaRepository<ShortLand, Long> {
}
