package com.github.yun531.climate.shortGrid.infra;

import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaShortGridRepository extends JpaRepository<ShortGrid, Long> {
    List<ShortGrid> findByAnnounceTimeAndXAndY(AnnounceTime announceTime, Integer x, Integer y);
}
