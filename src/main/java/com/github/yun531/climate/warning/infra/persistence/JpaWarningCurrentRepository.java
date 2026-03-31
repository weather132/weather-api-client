package com.github.yun531.climate.warning.infra.persistence;

import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import com.github.yun531.climate.warning.domain.model.WarningId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaWarningCurrentRepository extends JpaRepository<WarningCurrent, WarningId> {
}