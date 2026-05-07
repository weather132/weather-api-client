package com.github.yun531.climate.warning.domain.warningEvent;

import java.util.List;

public interface WarningEventRepository {
    void saveAll(List<WarningEvent> events);
    List<WarningEvent> findLatestByWarningRegionCodes(List<String> warningRegionCodes);
    List<WarningCurrent> findActiveWarnings();
}