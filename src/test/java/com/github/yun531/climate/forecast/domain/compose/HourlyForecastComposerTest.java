package com.github.yun531.climate.forecast.domain.compose;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.Coordinates;
import com.github.yun531.climate.forecast.domain.compose.HourlyForecastComposer.HourlyComposeResult;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyPoint;
import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import com.github.yun531.climate.shortGrid.domain.ShortGridRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HourlyForecastComposerTest {

    @Mock ShortGridRepository shortGridRepository;
    @Mock CityRegionCode regionCode;
    @Mock Coordinates coordinates;

    private HourlyForecastComposer composer;

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 3, 28, 14, 0);

    private static final Integer[] LATEST_POPS = {
            0, 0, 30, 70, 80, 70, 30, 0, 0, 0,
            0, 0, 70, 80, 70, 0, 0, 60, 70, 80,
            60, 0, 0, 0, 0, 0
    };

    @BeforeEach
    void setUp() {
        composer = new HourlyForecastComposer(shortGridRepository);
        when(regionCode.getCoordinates()).thenReturn(coordinates);
        when(coordinates.getX()).thenReturn(60);
        when(coordinates.getY()).thenReturn(127);
    }


    @Nested
    @DisplayName("POP 패턴 변환")
    class PopPatternConversion {

        @Test
        @DisplayName("POP 패턴의 ShortGrid → ForecastHourlyPoint 변환, 26개 보존")
        void converts_shortGrids_to_forecastHourlyPoints() {
            stubShortGrids(LATEST_POPS);

            HourlyComposeResult result = composer.compose(regionCode);

            assertThat(result.forecastHourlyPoints()).hasSize(26);
            assertThat(result.forecastHourlyPoints())
                    .extracting(ForecastHourlyPoint::pop)
                    .containsExactly(LATEST_POPS);
        }

        @Test
        @DisplayName("temp 값이 순서대로 매핑")
        void temp_values_mapped() {
            stubShortGrids(LATEST_POPS);

            HourlyComposeResult result = composer.compose(regionCode);

            assertThat(result.forecastHourlyPoints())
                    .extracting(ForecastHourlyPoint::temp)
                    .containsExactly(IntStream.rangeClosed(1, 26).boxed().toArray(Integer[]::new));
        }

        @Test
        @DisplayName("effectiveTime이 순서대로 매핑")
        void effectiveTime_values_mapped() {
            stubShortGrids(LATEST_POPS);

            HourlyComposeResult result = composer.compose(regionCode);

            assertThat(result.forecastHourlyPoints())
                    .extracting(ForecastHourlyPoint::effectiveTime)
                    .containsExactly(IntStream.rangeClosed(1, 26)
                            .mapToObj(ANNOUNCE_TIME::plusHours)
                            .toArray(LocalDateTime[]::new));
        }
    }


    @Nested
    @DisplayName("HourlyComposeResult")
    class ComposeResult {

        @Test
        @DisplayName("announceTime이 ShortGrid의 announceTime 에서 추출됨")
        void announceTime_extracted_from_shortGrid() {
            stubShortGrids(LATEST_POPS);

            HourlyComposeResult result = composer.compose(regionCode);

            assertThat(result.announceTime()).isEqualTo(ANNOUNCE_TIME);
        }

        @Test
        @DisplayName("빈 리스트 → announceTime null, forecastHourlyPoints 빈 리스트")
        void empty_repository_result() {
            when(shortGridRepository.findRecentByXAndY(eq(60), eq(127)))
                    .thenReturn(List.of());

            HourlyComposeResult result = composer.compose(regionCode);

            assertSoftly(softly -> {
                softly.assertThat(result.announceTime()).isNull();
                softly.assertThat(result.forecastHourlyPoints()).isEmpty();
            });
        }
    }


    @Nested
    @DisplayName("null 필터, 시간순 정렬, 26개 제한")
    class FilterSortLimit {

        @Test
        @DisplayName("effectiveTime null인 ShortGrid는 제외")
        void null_effectiveTime_filtered() {
            AnnounceTime at = new AnnounceTime(ANNOUNCE_TIME);
            List<ShortGrid> grids = new ArrayList<>();
            grids.add(new ShortGrid(at, null, 60, 127, 50, 10));
            grids.add(new ShortGrid(at, ANNOUNCE_TIME.plusHours(1), 60, 127, 30, 5));
            grids.add(new ShortGrid(at, null, 60, 127, 60, 15));

            when(shortGridRepository.findRecentByXAndY(eq(60), eq(127)))
                    .thenReturn(grids);

            HourlyComposeResult result = composer.compose(regionCode);

            assertThat(result.forecastHourlyPoints()).hasSize(1);
            assertThat(result.forecastHourlyPoints().get(0).effectiveTime())
                    .isEqualTo(ANNOUNCE_TIME.plusHours(1));
        }

        @Test
        @DisplayName("effectiveTime 기준 시간순 정렬")
        void sorted_by_effectiveTime() {
            AnnounceTime at = new AnnounceTime(ANNOUNCE_TIME);
            List<ShortGrid> grids = new ArrayList<>();
            grids.add(new ShortGrid(at, ANNOUNCE_TIME.plusHours(3), 60, 127, 30, 3));
            grids.add(new ShortGrid(at, ANNOUNCE_TIME.plusHours(1), 60, 127, 10, 1));
            grids.add(new ShortGrid(at, ANNOUNCE_TIME.plusHours(2), 60, 127, 20, 2));

            when(shortGridRepository.findRecentByXAndY(eq(60), eq(127)))
                    .thenReturn(grids);

            HourlyComposeResult result = composer.compose(regionCode);

            assertThat(result.forecastHourlyPoints())
                    .extracting(ForecastHourlyPoint::effectiveTime)
                    .isSorted();
        }

        @Test
        @DisplayName("30개 입력 → 26개로 제한, null 제외 후 정렬 결합")
        void combined_filter_sort_limit() {
            AnnounceTime at = new AnnounceTime(ANNOUNCE_TIME);
            List<ShortGrid> grids = new ArrayList<>();
            grids.add(new ShortGrid(at, null, 60, 127, 50, 10));
            for (int i = 1; i <= 30; i++) {
                grids.add(new ShortGrid(at, ANNOUNCE_TIME.plusHours(i), 60, 127, 10, i));
            }
            Collections.shuffle(grids);

            when(shortGridRepository.findRecentByXAndY(eq(60), eq(127)))
                    .thenReturn(grids);

            HourlyComposeResult result = composer.compose(regionCode);

            assertSoftly(softly -> {
                softly.assertThat(result.forecastHourlyPoints()).hasSize(26);
                softly.assertThat(result.forecastHourlyPoints())
                        .extracting(ForecastHourlyPoint::effectiveTime)
                        .isSorted();
                softly.assertThat(result.forecastHourlyPoints())
                        .allMatch(p -> p.effectiveTime() != null);
                softly.assertThat(result.forecastHourlyPoints().get(0).effectiveTime())
                        .isEqualTo(ANNOUNCE_TIME.plusHours(1));
            });
        }
    }

    // ==================== helper ====================

    private void stubShortGrids(Integer[] pops) {
        AnnounceTime at = new AnnounceTime(ANNOUNCE_TIME);
        List<ShortGrid> grids = new ArrayList<>(pops.length);
        for (int i = 0; i < pops.length; i++) {
            grids.add(new ShortGrid(at, ANNOUNCE_TIME.plusHours(i + 1), 60, 127, pops[i], i + 1));
        }
        when(shortGridRepository.findRecentByXAndY(eq(60), eq(127)))
                .thenReturn(grids);
    }
}