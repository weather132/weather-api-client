package com.github.yun531.climate.snapshot.contract;

import org.springframework.lang.Nullable;

/**
 * 스냅샷 읽기 계약.
 * 외부 BC(notification, forecast)는 해당 인터페이스에만 의존.
 */
public interface SnapshotReader {

    @Nullable
    WeatherSnapshot loadCurrent(String regionId);

    @Nullable
    WeatherSnapshot loadPrevious(String regionId);
}