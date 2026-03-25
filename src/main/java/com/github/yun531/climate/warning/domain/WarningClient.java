package com.github.yun531.climate.warning.domain;

import com.github.yun531.climate.warning.domain.model.WarningCurrent;

import java.time.LocalDateTime;
import java.util.List;

public interface WarningClient {
    List<WarningCurrent> requestCurrentWarnings(LocalDateTime tm);
}