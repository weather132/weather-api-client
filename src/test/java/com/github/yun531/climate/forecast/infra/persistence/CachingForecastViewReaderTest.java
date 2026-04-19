package com.github.yun531.climate.forecast.infra.persistence;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.forecast.domain.compose.DailyForecastComposer;
import com.github.yun531.climate.forecast.domain.compose.DailyForecastComposer.DailyComposeResult;
import com.github.yun531.climate.forecast.domain.compose.HourlyForecastComposer;
import com.github.yun531.climate.forecast.domain.compose.HourlyForecastComposer.HourlyComposeResult;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastDailyPoint;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastDailyView;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyPoint;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyView;
import com.github.yun531.climate.forecast.infra.cache.ForecastCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CachingForecastViewReaderTest {

    @Mock ForecastCacheManager cache;
    @Mock HourlyForecastComposer hourlyComposer;
    @Mock DailyForecastComposer dailyComposer;
    @Mock CityRegionCodeRepository cityRegionCodeRepository;
    @Mock CityRegionCode cityRegionCode;

    private CachingForecastViewReader reader;

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 1, 22, 5, 0);

    @BeforeEach
    void setUp() {
        reader = new CachingForecastViewReader(cache, hourlyComposer, dailyComposer, cityRegionCodeRepository);
    }

    @Nested
    @DisplayName("loadHourly")
    class LoadHourly {

        @Test
        @DisplayName("캐시 히트 → Composer 미호출, 캐시 결과 반환")
        void cacheHit_returnsWithoutCompose() {
            ForecastHourlyView cached = buildHourlyView();
            when(cache.getHourly("R1")).thenReturn(cached);

            ForecastHourlyView result = reader.loadHourly("R1");

            assertThat(result).isSameAs(cached);
            verify(hourlyComposer, never()).compose(any());
        }

        @Test
        @DisplayName("캐시 미스 → Composer 호출 → 캐시 저장 → 결과 반환")
        void cacheMiss_composesAndCaches() {
            when(cache.getHourly("R1")).thenReturn(null);
            when(cityRegionCodeRepository.findByRegionCode("R1")).thenReturn(cityRegionCode);
            when(hourlyComposer.compose(cityRegionCode)).thenReturn(
                    new HourlyComposeResult(ANNOUNCE_TIME, List.of(
                            new ForecastHourlyPoint(ANNOUNCE_TIME.plusHours(1), 10, 20))));

            ForecastHourlyView result = reader.loadHourly("R1");

            assertThat(result).isNotNull();
            assertThat(result.regionId()).isEqualTo("R1");
            assertThat(result.announceTime()).isEqualTo(ANNOUNCE_TIME);
            assertThat(result.hourlyPoints()).hasSize(1);
            verify(cache).putHourly(eq("R1"), any());
        }

        @Test
        @DisplayName("미존재 regionId → null, Composer 미호출")
        void unknownRegion_returnsNull() {
            when(cache.getHourly("R1")).thenReturn(null);
            when(cityRegionCodeRepository.findByRegionCode("R1")).thenReturn(null);

            assertThat(reader.loadHourly("R1")).isNull();
            verify(hourlyComposer, never()).compose(any());
        }

        @Test
        @DisplayName("Composer가 빈 포인트 반환 → null, 캐시 미저장")
        void emptyCompose_returnsNull() {
            when(cache.getHourly("R1")).thenReturn(null);
            when(cityRegionCodeRepository.findByRegionCode("R1")).thenReturn(cityRegionCode);
            when(hourlyComposer.compose(cityRegionCode)).thenReturn(
                    new HourlyComposeResult(null, List.of()));

            assertThat(reader.loadHourly("R1")).isNull();
            verify(cache, never()).putHourly(any(), any());
        }
    }


    @Nested
    @DisplayName("loadDaily")
    class LoadDaily {

        @Test
        @DisplayName("캐시 히트 → Composer 미호출, 캐시 결과 반환")
        void cacheHit_returnsWithoutCompose() {
            ForecastDailyView cached = buildDailyView();
            when(cache.getDaily("R1")).thenReturn(cached);

            ForecastDailyView result = reader.loadDaily("R1");

            assertThat(result).isSameAs(cached);
            verify(dailyComposer, never()).compose(any());
        }

        @Test
        @DisplayName("캐시 미스 → Composer 호출 → 캐시 저장 → 결과 반환")
        void cacheMiss_composesAndCaches() {
            when(cache.getDaily("R1")).thenReturn(null);
            when(cityRegionCodeRepository.findByRegionCode("R1")).thenReturn(cityRegionCode);
            when(dailyComposer.compose(cityRegionCode)).thenReturn(
                    new DailyComposeResult(ANNOUNCE_TIME, List.of(
                            new ForecastDailyPoint(0, -5, 5, 30, 60))));

            ForecastDailyView result = reader.loadDaily("R1");

            assertThat(result).isNotNull();
            assertThat(result.regionId()).isEqualTo("R1");
            assertThat(result.dailyPoints()).hasSize(1);
            verify(cache).putDaily(eq("R1"), any());
        }

        @Test
        @DisplayName("미존재 regionId → null, Composer 미호출")
        void unknownRegion_returnsNull() {
            when(cache.getDaily("R1")).thenReturn(null);
            when(cityRegionCodeRepository.findByRegionCode("R1")).thenReturn(null);

            assertThat(reader.loadDaily("R1")).isNull();
            verify(dailyComposer, never()).compose(any());
        }

        @Test
        @DisplayName("Composer가 빈 포인트 반환 → null, 캐시 미저장")
        void emptyCompose_returnsNull() {
            when(cache.getDaily("R1")).thenReturn(null);
            when(cityRegionCodeRepository.findByRegionCode("R1")).thenReturn(cityRegionCode);
            when(dailyComposer.compose(cityRegionCode)).thenReturn(
                    new DailyComposeResult(null, List.of()));

            assertThat(reader.loadDaily("R1")).isNull();
            verify(cache, never()).putDaily(any(), any());
        }
    }

    // ==================== helper ====================

    private ForecastHourlyView buildHourlyView() {
        return new ForecastHourlyView("R1", ANNOUNCE_TIME, List.of(
                new ForecastHourlyPoint(ANNOUNCE_TIME.plusHours(1), 10, 20)));
    }

    private ForecastDailyView buildDailyView() {
        return new ForecastDailyView("R1", ANNOUNCE_TIME, List.of(
                new ForecastDailyPoint(0, -5, 5, 30, 60)));
    }
}