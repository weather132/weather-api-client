package com.github.yun531.climate.shortGrid.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface ShortGridClient {
    List<ShortGrid> requestShortGrids(AnnounceTime announceTime, LocalDateTime effectiveTime);
    List<ShortGrid> requestShortGridsForHours(AnnounceTime announceTime, int hours);
}
