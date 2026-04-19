package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.readmodel.PopView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PopCacheManagerTest {

    private PopCacheManager cacheManager;

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 3, 28, 14, 0);

    @BeforeEach
    void setUp() {
        cacheManager = new PopCacheManager();
    }


    @Nested
    @DisplayName("기본 put/get")
    class PutGet {

        @Test
        @DisplayName("putCurrent 후 getCurrent — 동일 객체 반환")
        void putCurrentThenGet() {
            PopView view = buildPopView(ANNOUNCE_TIME);
            cacheManager.putCurrent("R1", view);

            assertThat(cacheManager.getCurrent("R1")).isSameAs(view);
        }

        @Test
        @DisplayName("미등록 regionId getCurrent → null")
        void currentMiss() {
            assertThat(cacheManager.getCurrent("R1")).isNull();
        }

        @Test
        @DisplayName("미등록 regionId getPrevious → null")
        void previousMiss() {
            assertThat(cacheManager.getPrevious("R1")).isNull();
        }

        @Test
        @DisplayName("여러 regionId 독립 저장")
        void multipleRegions() {
            PopView view1 = buildPopView(ANNOUNCE_TIME);
            PopView view2 = buildPopView(ANNOUNCE_TIME.plusHours(3));
            cacheManager.putCurrent("R1", view1);
            cacheManager.putCurrent("R2", view2);

            assertThat(cacheManager.getCurrent("R1")).isSameAs(view1);
            assertThat(cacheManager.getCurrent("R2")).isSameAs(view2);
        }
    }


    @Nested
    @DisplayName("rotate")
    class Rotate {

        @Test
        @DisplayName("rotate 후 기존 current가 previous로 이동")
        void currentMovesToPrevious() {
            PopView currentView = buildPopView(ANNOUNCE_TIME);
            cacheManager.putCurrent("R1", currentView);

            cacheManager.rotate();

            assertThat(cacheManager.getPrevious("R1")).isSameAs(currentView);
        }

        @Test
        @DisplayName("rotate 후 current는 비워짐")
        void currentCleared() {
            cacheManager.putCurrent("R1", buildPopView(ANNOUNCE_TIME));

            cacheManager.rotate();

            assertThat(cacheManager.getCurrent("R1")).isNull();
        }

        @Test
        @DisplayName("rotate 2회 — 이전 previous 사라짐, 직전 current만 previous에 존재")
        void doubleRotate() {
            PopView first = buildPopView(ANNOUNCE_TIME);
            cacheManager.putCurrent("R1", first);
            cacheManager.rotate();

            PopView second = buildPopView(ANNOUNCE_TIME.plusHours(3));
            cacheManager.putCurrent("R1", second);
            cacheManager.rotate();

            assertThat(cacheManager.getPrevious("R1"))
                    .as("2회 rotate 후 previous는 직전 current(second)")
                    .isSameAs(second);
            assertThat(cacheManager.getCurrent("R1")).isNull();
        }

        @Test
        @DisplayName("여러 regionId — rotate 시 전부 이동")
        void rotateAllRegions() {
            PopView view1 = buildPopView(ANNOUNCE_TIME);
            PopView view2 = buildPopView(ANNOUNCE_TIME);
            cacheManager.putCurrent("R1", view1);
            cacheManager.putCurrent("R2", view2);

            cacheManager.rotate();

            assertThat(cacheManager.getPrevious("R1")).isSameAs(view1);
            assertThat(cacheManager.getPrevious("R2")).isSameAs(view2);
            assertThat(cacheManager.getCurrent("R1")).isNull();
            assertThat(cacheManager.getCurrent("R2")).isNull();
        }

        @Test
        @DisplayName("빈 상태에서 rotate — 예외 없이 동작")
        void rotateEmpty() {
            cacheManager.rotate();

            assertThat(cacheManager.getCurrent("R1")).isNull();
            assertThat(cacheManager.getPrevious("R1")).isNull();
        }
    }

    // ==================== helper ====================

    private PopView buildPopView(LocalDateTime announceTime) {
        List<PopView.Hourly.Pop> hourlyPops = new ArrayList<>(PopView.HOURLY_SIZE);
        for (int i = 0; i < PopView.HOURLY_SIZE; i++) {
            hourlyPops.add(new PopView.Hourly.Pop(announceTime.plusHours(i + 1), i * 3));
        }

        List<PopView.Daily.Pop> dailyPops = new ArrayList<>(PopView.DAILY_SIZE);
        for (int i = 0; i < PopView.DAILY_SIZE; i++) {
            dailyPops.add(new PopView.Daily.Pop(i * 10, i * 10 + 5));
        }

        return new PopView(
                new PopView.Hourly(hourlyPops),
                new PopView.Daily(dailyPops),
                announceTime
        );
    }
}