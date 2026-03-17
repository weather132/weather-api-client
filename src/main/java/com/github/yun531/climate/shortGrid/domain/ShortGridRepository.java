package com.github.yun531.climate.shortGrid.domain;

import java.util.List;

public interface ShortGridRepository {
    void saveAll(List<ShortGrid> shortGrids);
    List<ShortGrid> findByAnnounceTimeAndXAndY(AnnounceTime announceTime, int x, int y);
}
