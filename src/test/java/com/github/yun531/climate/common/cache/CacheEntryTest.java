package com.github.yun531.climate.common.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CacheEntryTest {

    private static final LocalDateTime ANCHOR = LocalDateTime.of(2026, 1, 22, 5, 0);

    @Test
    @DisplayName("referenceTime이 anchor + tolerance 이내면 fresh")
    void withinTolerance_notStale() {
        CacheEntry<String> entry = new CacheEntry<>("v", ANCHOR);

        assertThat(entry.isStale(ANCHOR.plusMinutes(30), 60)).isFalse();
    }

    @Test
    @DisplayName("referenceTime이 anchor + tolerance 초과면 stale")
    void exceedsTolerance_stale() {
        CacheEntry<String> entry = new CacheEntry<>("v", ANCHOR);

        assertThat(entry.isStale(ANCHOR.plusMinutes(61), 60)).isTrue();
    }

    @Test
    @DisplayName("경계값: 정확히 tolerance 시점은 fresh")
    void exactBoundary_notStale() {
        CacheEntry<String> entry = new CacheEntry<>("v", ANCHOR);

        assertThat(entry.isStale(ANCHOR.plusMinutes(60), 60)).isFalse();
    }

    @Test
    @DisplayName("referenceTime이 null 이면 항상 stale")
    void nullReferenceTime_stale() {
        CacheEntry<String> entry = new CacheEntry<>("v", ANCHOR);

        assertThat(entry.isStale(null, 60)).isTrue();
    }

    @Test
    @DisplayName("anchor가 null 이면 항상 stale")
    void nullAnchor_stale() {
        CacheEntry<String> entry = new CacheEntry<>("v", null);

        assertThat(entry.isStale(ANCHOR, 60)).isTrue();
    }
}
