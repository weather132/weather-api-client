package com.github.yun531.climate.warning.domain.repository;

import com.github.yun531.climate.warning.domain.model.WarningCurrent;

import java.util.List;

public interface WarningCurrentRepository {
    void replaceAll(List<WarningCurrent> currents);
    List<WarningCurrent> findAll();
}