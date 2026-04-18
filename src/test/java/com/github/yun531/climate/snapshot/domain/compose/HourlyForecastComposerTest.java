//package com.github.yun531.climate.snapshot.domain.compose;
//
//import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
//import com.github.yun531.climate.cityRegionCode.domain.Coordinates;
//import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
//import com.github.yun531.climate.shortGrid.domain.ShortGrid;
//import com.github.yun531.climate.shortGrid.domain.ShortGridRepository;
//import com.github.yun531.climate.snapshot.domain.model.HourlyForecastItem;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.IntStream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class HourlyForecastComposerTest {
//
//    @Mock ShortGridRepository shortGridRepository;
//    @Mock CityRegionCode regionCode;
//    @Mock Coordinates coordinates;
//
//    private HourlyForecastComposer composer;
//
//    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 3, 28, 14, 0);
//
//    private static final Integer[] LATEST_POPS = {
//            0, 0, 30, 70, 80, 70, 30, 0, 0, 0,
//            0, 0, 70, 80, 70, 0, 0, 60, 70, 80,
//            60, 0, 0, 0, 0, 0
//    };
//
//    @BeforeEach
//    void setUp() {
//        composer = new HourlyForecastComposer(shortGridRepository);
//        when(regionCode.getCoordinates()).thenReturn(coordinates);
//        when(coordinates.getX()).thenReturn(60);
//        when(coordinates.getY()).thenReturn(127);
//    }
//
//    @Test
//    @DisplayName("POP 패턴의 ShortGrid → HourlyForecastItem 변환")
//    void converts_shortGrids_to_hourlyForecastItems() {
//        stubShortGrids(LATEST_POPS);
//
//        List<HourlyForecastItem> result = composer.compose(regionCode, ANNOUNCE_TIME);
//
//        assertThat(result).hasSize(26);
//        assertThat(result).extracting(HourlyForecastItem::getPop)
//                .containsExactly(LATEST_POPS);
//        assertThat(result).extracting(HourlyForecastItem::getTemp)
//                .containsExactly(expectedTemps());
//        assertThat(result).extracting(HourlyForecastItem::getEffectiveTime)
//                .containsExactly(expectedTimes());
//    }
//
//    @Test
//    @DisplayName("repository가 빈 리스트 반환 시 빈 리스트 출력")
//    void empty_repository_result() {
//        stubShortGrids(new Integer[0]);
//
//        List<HourlyForecastItem> result = composer.compose(regionCode, ANNOUNCE_TIME);
//
//        assertThat(result).isEmpty();
//    }
//
//    // ==================== helper ====================
//
//    private void stubShortGrids(Integer[] pops) {
//        List<ShortGrid> grids = createShortGrids(pops);
//        when(shortGridRepository.findByAnnounceTimeAndXAndY(
//                any(AnnounceTime.class), eq(60), eq(127)))
//                .thenReturn(grids);
//    }
//
//    private List<ShortGrid> createShortGrids(Integer[] pops) {
//        AnnounceTime at = new AnnounceTime(ANNOUNCE_TIME);
//        List<ShortGrid> grids = new ArrayList<>(pops.length);
//        for (int i = 0; i < pops.length; i++) {
//            grids.add(new ShortGrid(at, ANNOUNCE_TIME.plusHours(i + 1), 60, 127, pops[i], i + 1));
//        }
//        return grids;
//    }
//
//    private Integer[] expectedTemps() {
//        return IntStream.rangeClosed(1, 26).boxed().toArray(Integer[]::new);
//    }
//
//    private LocalDateTime[] expectedTimes() {
//        return IntStream.rangeClosed(1, 26)
//                .mapToObj(ANNOUNCE_TIME::plusHours)
//                .toArray(LocalDateTime[]::new);
//    }
//}