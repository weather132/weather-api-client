package com.github.yun531.climate.snapshot.infra.reader;

import com.github.yun531.climate.common.cache.CacheEntry;
import com.github.yun531.climate.common.cache.KeyCache;
import com.github.yun531.climate.common.time.TimeUtil;
import com.github.yun531.climate.snapshot.contract.SnapshotReader;
import com.github.yun531.climate.snapshot.contract.WeatherSnapshot;
import com.github.yun531.climate.snapshot.domain.model.SnapKind;
import com.github.yun531.climate.snapshot.domain.policy.PublishSchedulePolicy;
import com.github.yun531.climate.snapshot.infra.config.SnapshotCacheProperties;
import org.springframework.lang.Nullable;

import java.time.Clock;
import java.time.LocalDateTime;

public abstract class CachingSnapshotReader implements SnapshotReader {

    private final SnapshotCacheProperties cacheProps;
    private final PublishSchedulePolicy publishSchedule;
    private final Clock clock;

    private final KeyCache<WeatherSnapshot> snapshotCache = new KeyCache<>();

    protected CachingSnapshotReader(
            SnapshotCacheProperties cacheProps,
            PublishSchedulePolicy publishSchedule,
            Clock clock
    ) {
        this.cacheProps = cacheProps;
        this.publishSchedule = publishSchedule;
        this.clock = clock;
    }

    @Override
    @Nullable
    public WeatherSnapshot loadCurrent(String regionId) {
        return load(regionId, SnapKind.CURRENT);
    }

    @Override
    @Nullable
    public WeatherSnapshot loadPrevious(String regionId) {
        return load(regionId, SnapKind.PREVIOUS);
    }

    private WeatherSnapshot load(String regionId, SnapKind kind) {
        if (regionId == null || regionId.isBlank() || kind == null) return null;

        LocalDateTime now = now();
        LocalDateTime announceTime = publishSchedule.announceTimeFor(now, kind);
        if (announceTime == null) return null;

        SnapshotKey key = SnapshotKey.of(regionId, kind);

        CacheEntry<WeatherSnapshot> entry = snapshotCache.getOrCompute(
                key.asCacheKey(),
                announceTime,
                cacheProps.recomputeThresholdMinutes(),
                () -> doCompose(key, announceTime)
        );

        return (entry == null) ? null : entry.value();
    }

    /**
     * 실제 스냅샷 조합을 수행한다.
     */
    protected abstract CacheEntry<WeatherSnapshot> doCompose(
            SnapshotKey key, LocalDateTime announceTime);

    private LocalDateTime now() {
        return TimeUtil.truncateToMinutes(LocalDateTime.now(clock));
    }
}