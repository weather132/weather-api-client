package com.github.yun531.climate.forecast.infra.cache;

import com.github.yun531.climate.forecast.domain.readmodel.FcstDailyPoint;
import com.github.yun531.climate.forecast.domain.readmodel.FcstDailyView;
import com.github.yun531.climate.forecast.domain.readmodel.FcstHourlyPoint;
import com.github.yun531.climate.forecast.domain.readmodel.FcstHourlyView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FcstCacheManagerTest {

    private FcstCacheManager cacheManager;

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 3, 28, 14, 0);

    @BeforeEach
    void setUp() {
        cacheManager = new FcstCacheManager();
    }

    @Nested
    @DisplayName("Hourly 캐시")
    class HourlyCache {

        @Test
        @DisplayName("put 후 get — 동일 객체 반환")
        void putThenGet() {
            FcstHourlyView view = buildHourlyView("R1");
            cacheManager.putHourly("R1", view);

            assertThat(cacheManager.getHourly("R1")).isSameAs(view);
        }

        @Test
        @DisplayName("미등록 regionId → null")
        void miss() {
            assertThat(cacheManager.getHourly("R1")).isNull();
        }

        @Test
        @DisplayName("invalidateHourly 후 모든 hourly 캐시 미스")
        void invalidateHourly_clearsAll() {
            cacheManager.putHourly("R1", buildHourlyView("R1"));
            cacheManager.putHourly("R2", buildHourlyView("R2"));

            cacheManager.invalidateHourly();

            assertThat(cacheManager.getHourly("R1")).isNull();
            assertThat(cacheManager.getHourly("R2")).isNull();
        }

        @Test
        @DisplayName("invalidateDaily는 hourly 캐시에 영향 없음")
        void invalidateDaily_doesNotAffectHourly() {
            cacheManager.putHourly("R1", buildHourlyView("R1"));

            cacheManager.invalidateDaily();

            assertThat(cacheManager.getHourly("R1")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Daily 캐시")
    class DailyCache {

        @Test
        @DisplayName("put 후 get — 동일 객체 반환")
        void putThenGet() {
            FcstDailyView view = buildDailyView("R1");
            cacheManager.putDaily("R1", view);

            assertThat(cacheManager.getDaily("R1")).isSameAs(view);
        }

        @Test
        @DisplayName("미등록 regionId → null")
        void miss() {
            assertThat(cacheManager.getDaily("R1")).isNull();
        }

        @Test
        @DisplayName("invalidateDaily 후 모든 daily 캐시 미스")
        void invalidateDaily_clearsAll() {
            cacheManager.putDaily("R1", buildDailyView("R1"));
            cacheManager.putDaily("R2", buildDailyView("R2"));

            cacheManager.invalidateDaily();

            assertThat(cacheManager.getDaily("R1")).isNull();
            assertThat(cacheManager.getDaily("R2")).isNull();
        }

        @Test
        @DisplayName("invalidateHourly는 daily 캐시에 영향 없음")
        void invalidateHourly_doesNotAffectDaily() {
            cacheManager.putDaily("R1", buildDailyView("R1"));

            cacheManager.invalidateHourly();

            assertThat(cacheManager.getDaily("R1")).isNotNull();
        }
    }

    // ==================== helper ====================

    private FcstHourlyView buildHourlyView(String regionId) {
        return new FcstHourlyView(regionId, ANNOUNCE_TIME, List.of(
                new FcstHourlyPoint(ANNOUNCE_TIME.plusHours(1), 10, 20)));
    }

    private FcstDailyView buildDailyView(String regionId) {
        return new FcstDailyView(regionId, ANNOUNCE_TIME, List.of(
                new FcstDailyPoint(0, -5, 5, 30, 60)));
    }
}