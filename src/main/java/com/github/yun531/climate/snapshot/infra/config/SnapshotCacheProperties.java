package com.github.yun531.climate.snapshot.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "snapshot.cache")
public record SnapshotCacheProperties(
        int recomputeThresholdMinutes
) {
    public SnapshotCacheProperties {
        if (recomputeThresholdMinutes <= 0) recomputeThresholdMinutes = 165;
    }
}