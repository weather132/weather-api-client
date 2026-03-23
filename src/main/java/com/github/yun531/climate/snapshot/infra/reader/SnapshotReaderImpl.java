package com.github.yun531.climate.snapshot.infra.reader;

import com.github.yun531.climate.common.cache.CacheEntry;
import com.github.yun531.climate.snapshot.contract.WeatherSnapshot;
import com.github.yun531.climate.snapshot.domain.compose.SnapshotComposeService;
import com.github.yun531.climate.snapshot.domain.policy.PublishSchedulePolicy;
import com.github.yun531.climate.snapshot.infra.config.SnapshotCacheProperties;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class SnapshotReaderImpl extends CachingSnapshotReader {

    private final SnapshotComposeService composeService;

    public SnapshotReaderImpl(
            SnapshotCacheProperties cacheProps,
            PublishSchedulePolicy publishSchedule,
            Clock clock,
            SnapshotComposeService composeService
    ) {
        super(cacheProps, publishSchedule, clock);
        this.composeService = composeService;
    }

    @Override
    protected CacheEntry<WeatherSnapshot> doCompose(
            SnapshotKey key, LocalDateTime announceTime
    ) {
        WeatherSnapshot snapshot = composeService.composeSnapshot(key.regionId(), announceTime);

        if (snapshot == null) return null;

        return new CacheEntry<>(snapshot, snapshot.announceTime());
    }
}