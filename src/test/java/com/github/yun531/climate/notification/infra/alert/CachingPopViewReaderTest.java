package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.readmodel.PopView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CachingPopViewReaderTest {

    @Mock PopCacheManager cache;
    @Mock PopViewComposer composer;

    private CachingPopViewReader reader;

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 1, 22, 5, 0);

    @BeforeEach
    void setUp() {
        reader = new CachingPopViewReader(cache, composer);
    }


    @Nested
    @DisplayName("loadCurrent")
    class LoadCurrent {

        @Test
        @DisplayName("캐시 히트 → Composer 미호출, 캐시 결과 반환")
        void cacheHit_returnsWithoutCompose() {
            PopView cached = buildPopView();
            when(cache.getCurrent("R1")).thenReturn(cached);

            PopView result = reader.loadCurrent("R1");

            assertThat(result).isSameAs(cached);
            verify(composer, never()).compose(any());
        }

        @Test
        @DisplayName("캐시 미스 → Composer 호출 → 캐시 저장 → 결과 반환")
        void cacheMiss_composesAndCaches() {
            PopView composed = buildPopView();
            when(cache.getCurrent("R1")).thenReturn(null);
            when(composer.compose("R1")).thenReturn(composed);

            PopView result = reader.loadCurrent("R1");

            assertThat(result).isSameAs(composed);
            verify(cache).putCurrent("R1", composed);
        }

        @Test
        @DisplayName("Composer가 null 반환 → null, 캐시 미저장")
        void composeReturnsNull_noCachePut() {
            when(cache.getCurrent("R1")).thenReturn(null);
            when(composer.compose("R1")).thenReturn(null);

            assertThat(reader.loadCurrent("R1")).isNull();
            verify(cache, never()).putCurrent(any(), any());
        }
    }


    @Nested
    @DisplayName("loadPrevious")
    class LoadPrevious {

        @Test
        @DisplayName("previous 캐시에 데이터 있음 → 반환")
        void previousExists() {
            PopView previousView = buildPopView();
            when(cache.getPrevious("R1")).thenReturn(previousView);

            assertThat(reader.loadPrevious("R1")).isSameAs(previousView);
        }

        @Test
        @DisplayName("previous 캐시 비어있음 (서버 재시작 직후) → null, Composer 미호출")
        void previousMiss_returnsNull_noCompose() {
            when(cache.getPrevious("R1")).thenReturn(null);

            assertThat(reader.loadPrevious("R1")).isNull();
            verify(composer, never()).compose(any());
        }
    }


    @Nested
    @DisplayName("loadCurrentPreviousPair")
    class LoadPair {

        @Test
        @DisplayName("current + previous 모두 존재 → Pair 반환")
        void bothExist() {
            PopView currentView = buildPopView();
            PopView previousView = buildPopView();

            when(cache.getCurrent("R1")).thenReturn(currentView);
            when(cache.getPrevious("R1")).thenReturn(previousView);

            PopView.Pair pair = reader.loadCurrentPreviousPair("R1");

            assertThat(pair).isNotNull();
            assertThat(pair.current()).isSameAs(currentView);
            assertThat(pair.previous()).isSameAs(previousView);
        }

        @Test
        @DisplayName("current null → null Pair")
        void currentNull() {
            when(cache.getCurrent("R1")).thenReturn(null);
            when(composer.compose("R1")).thenReturn(null);

            assertThat(reader.loadCurrentPreviousPair("R1")).isNull();
        }

        @Test
        @DisplayName("previous null → null Pair")
        void previousNull() {
            PopView currentView = buildPopView();
            when(cache.getCurrent("R1")).thenReturn(currentView);
            when(cache.getPrevious("R1")).thenReturn(null);

            assertThat(reader.loadCurrentPreviousPair("R1")).isNull();
        }
    }

    // ==================== helper ====================

    private PopView buildPopView() {
        List<PopView.Hourly.Pop> hourlyPops = new ArrayList<>(PopView.HOURLY_SIZE);
        for (int i = 0; i < PopView.HOURLY_SIZE; i++) {
            hourlyPops.add(new PopView.Hourly.Pop(ANNOUNCE_TIME.plusHours(i + 1), i * 3));
        }

        List<PopView.Daily.Pop> dailyPops = new ArrayList<>(PopView.DAILY_SIZE);
        for (int i = 0; i < PopView.DAILY_SIZE; i++) {
            dailyPops.add(new PopView.Daily.Pop(i * 10, i * 10 + 5));
        }

        return new PopView(
                new PopView.Hourly(hourlyPops),
                new PopView.Daily(dailyPops),
                ANNOUNCE_TIME
        );
    }
}