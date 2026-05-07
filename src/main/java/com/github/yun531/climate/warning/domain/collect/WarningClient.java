package com.github.yun531.climate.warning.domain.collect;

import com.github.yun531.climate.warning.domain.warningEvent.WarningCurrent;

import java.time.LocalDateTime;
import java.util.List;

public interface WarningClient {
    List<WarningCurrent> requestCurrentWarnings(LocalDateTime tm);
}