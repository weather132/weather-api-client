package com.github.yun531.climate.common.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeyCacheTest {

    private static final String KEY = "key";
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 0, 0);

    private KeyCache<String> cache;
    private AtomicInteger loaderCallCount;

    @BeforeEach
    void setUp() {
        cache = new KeyCache<>();
        loaderCallCount = new AtomicInteger();
    }

    @Test
    @DisplayName("엔트리가 없으면 loader를 호출한다")
    void noEntry_loaderCalled() {
        CacheEntry<String> result = compute("v1", T0);

        assertThat(result.value()).isEqualTo("v1");
        assertThat(loaderCallCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("엔트리가 fresh면 loader를 호출하지 않고 기존 값을 반환한다")
    void freshEntry_loaderNotCalled() {
        compute("v1", T0);
        CacheEntry<String> result = compute("v2", T0);

        assertThat(result.value()).isEqualTo("v1");
        assertThat(loaderCallCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("엔트리가 stale 이면 loader를 재호출한다")
    void staleEntry_loaderRecalled() {
        compute("v1", T0);
        CacheEntry<String> result = compute("v2", null);  // null -> 확실히 stale

        assertThat(result.value()).isEqualTo("v2");
        assertThat(loaderCallCount.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("loader가 null 이면 IllegalArgumentException")
    void nullLoader_throws() {
        assertThatThrownBy(() -> cache.getOrCompute(KEY, T0, 60, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ==================== helper ====================

    private CacheEntry<String> compute(String value, LocalDateTime referenceTime) {
        return cache.getOrCompute(KEY, referenceTime, 60, () -> {
            loaderCallCount.incrementAndGet();
            return new CacheEntry<>(value, T0);
        });
    }
}