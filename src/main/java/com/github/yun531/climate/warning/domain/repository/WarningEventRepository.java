package com.github.yun531.climate.warning.domain.repository;

import com.github.yun531.climate.warning.domain.model.WarningEvent;

import java.util.List;

public interface WarningEventRepository {
    void saveAll(List<WarningEvent> events);
    List<WarningEvent> findLatestByWarningRegionCodes(List<String> warningRegionCodes);
}