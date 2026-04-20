package com.github.yun531.climate.notification.domain.compose;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.cityRegionCode.domain.Coordinates;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandRepository;
import com.github.yun531.climate.notification.domain.readmodel.PopView;
import com.github.yun531.climate.notification.domain.readmodel.PopView.Daily;
import com.github.yun531.climate.notification.domain.readmodel.PopView.Hourly;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCodeRepository;
import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import com.github.yun531.climate.shortGrid.domain.ShortGridRepository;
import com.github.yun531.climate.shortLand.domain.ShortLand;
import com.github.yun531.climate.shortLand.domain.ShortLandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PopViewComposerTest {

    @Mock ShortGridRepository shortGridRepository;
    @Mock ShortLandRepository shortLandRepository;
    @Mock MidLandRepository midLandRepository;
    @Mock ProvinceRegionCodeRepository provinceRegionCodeRepository;
    @Mock CityRegionCodeRepository cityRegionCodeRepository;
    @Mock CityRegionCode cityRegionCode;
    @Mock Coordinates coordinates;
    @Mock ProvinceRegionCode provinceRegionCode;

    private PopViewComposer composer;

    private static final String REGION_ID = "R1";
    private static final Long CITY_ID     = 1L;
    private static final Long PROVINCE_ID = 100L;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 28, 14, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            NOW.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    private static final LocalDateTime GRID_ANNOUNCE_TIME =
            LocalDateTime.of(2026, 3, 28, 14, 0);
    private static final LocalDateTime SHORT_LAND_ANNOUNCE_TIME =
            LocalDateTime.of(2026, 3, 28, 17, 0);

    @BeforeEach
    void setUp() {
        composer = new PopViewComposer(
                shortGridRepository, shortLandRepository, midLandRepository,
                provinceRegionCodeRepository, cityRegionCodeRepository, FIXED_CLOCK);

        lenient().when(cityRegionCodeRepository.findByRegionCode(REGION_ID)).thenReturn(cityRegionCode);
        lenient().when(cityRegionCode.getCoordinates()).thenReturn(coordinates);
        lenient().when(coordinates.getX()).thenReturn(60);
        lenient().when(coordinates.getY()).thenReturn(127);
        lenient().when(cityRegionCode.getId()).thenReturn(CITY_ID);
        lenient().when(cityRegionCode.getProvinceRegionCodeId()).thenReturn(PROVINCE_ID);
    }


    @Nested
    @DisplayName("compose 진입점")
    class Compose {

        @Test
        @DisplayName("존재하지 않는 regionId → null 반환")
        void unknownRegionId_returnsNull() {
            when(cityRegionCodeRepository.findByRegionCode("UNKNOWN")).thenReturn(null);

            assertThat(composer.compose("UNKNOWN")).isNull();
        }

        @Test
        @DisplayName("정상 regionId → PopView 반환, Hourly/Daily 모두 포함")
        void validRegionId_returnsPopView() {
            stubShortGrids(26);
            stubShortLandBatch(50);

            PopView result = composer.compose(REGION_ID);

            assertSoftly(softly -> {
                softly.assertThat(result).isNotNull();
                softly.assertThat(result.hourly().pops()).hasSize(PopView.HOURLY_SIZE);
                softly.assertThat(result.daily().pops()).hasSize(PopView.DAILY_SIZE);
            });
        }
    }


    @Nested
    @DisplayName("Hourly 경로")
    class HourlyPath {

        @Test
        @DisplayName("ShortGrid 26개 → Hourly POP 26개 매핑")
        void fullShortGrids_allMapped() {
            stubShortGrids(26);
            stubShortLandBatch(50);

            PopView result = composer.compose(REGION_ID);

            assertThat(result.hourly().pops())
                    .extracting(Hourly.Pop::pop)
                    .hasSize(26)
                    .doesNotContainNull();
        }

        @Test
        @DisplayName("ShortGrid 10개 → 나머지 16개 empty 패딩")
        void partialShortGrids_paddedWithEmpty() {
            stubShortGrids(10);
            stubShortLandBatch(50);

            PopView result = composer.compose(REGION_ID);
            List<Hourly.Pop> pops = result.hourly().pops();

            assertSoftly(softly -> {
                softly.assertThat(pops).hasSize(26);
                // 앞 10개는 데이터 존재
                softly.assertThat(pops.subList(0, 10))
                        .extracting(Hourly.Pop::pop)
                        .doesNotContainNull();
                // 뒤 16개는 empty
                softly.assertThat(pops.subList(10, 26))
                        .allSatisfy(p -> {
                            softly.assertThat(p.effectiveTime()).isNull();
                            softly.assertThat(p.pop()).isNull();
                        });
            });
        }

        @Test
        @DisplayName("ShortGrid 빈 리스트 → 26개 전부 empty, announceTime null")
        void emptyShortGrids_allEmpty() {
            when(shortGridRepository.findRecentByXAndY(eq(60), eq(127)))
                    .thenReturn(List.of());
            stubShortLandBatch(50);

            PopView result = composer.compose(REGION_ID);

            assertSoftly(softly -> {
                softly.assertThat(result.announceTime()).isNull();
                softly.assertThat(result.hourly().pops())
                        .allSatisfy(p -> {
                            softly.assertThat(p.effectiveTime()).isNull();
                            softly.assertThat(p.pop()).isNull();
                        });
            });
        }

        @Test
        @DisplayName("announceTime이 ShortGrid에서 추출됨")
        void announceTime_fromShortGrid() {
            stubShortGrids(26);
            stubShortLandBatch(50);

            PopView result = composer.compose(REGION_ID);

            assertThat(result.announceTime()).isEqualTo(GRID_ANNOUNCE_TIME);
        }

        @Test
        @DisplayName("effectiveTime 기준 시간순 정렬")
        void sortedByEffectiveTime() {
            AnnounceTime at = new AnnounceTime(GRID_ANNOUNCE_TIME);
            List<ShortGrid> grids = new ArrayList<>();
            grids.add(new ShortGrid(at, GRID_ANNOUNCE_TIME.plusHours(3), 60, 127, 30, 3));
            grids.add(new ShortGrid(at, GRID_ANNOUNCE_TIME.plusHours(1), 60, 127, 10, 1));
            grids.add(new ShortGrid(at, GRID_ANNOUNCE_TIME.plusHours(2), 60, 127, 20, 2));
            when(shortGridRepository.findRecentByXAndY(eq(60), eq(127))).thenReturn(grids);
            stubShortLandBatch(50);

            PopView result = composer.compose(REGION_ID);

            assertThat(result.hourly().pops().subList(0, 3))
                    .extracting(Hourly.Pop::pop)
                    .containsExactly(10, 20, 30);
        }

        @Test
        @DisplayName("effectiveTime null인 ShortGrid는 제외")
        void nullEffectiveTime_filtered() {
            AnnounceTime at = new AnnounceTime(GRID_ANNOUNCE_TIME);
            List<ShortGrid> grids = new ArrayList<>();
            grids.add(new ShortGrid(at, null, 60, 127, 50, 10));
            grids.add(new ShortGrid(at, GRID_ANNOUNCE_TIME.plusHours(1), 60, 127, 30, 5));
            when(shortGridRepository.findRecentByXAndY(eq(60), eq(127))).thenReturn(grids);
            stubShortLandBatch(50);

            PopView result = composer.compose(REGION_ID);

            // null 제외 후 1개 데이터 + 25개 empty
            assertThat(result.hourly().pops().get(0).pop()).isEqualTo(30);
            assertThat(result.hourly().pops().get(1).pop()).isNull();
        }
    }


    @Nested
    @DisplayName("Daily - ShortLand 경로")
    class DailyShortLandPath {

        @Test
        @DisplayName("ShortLand 데이터 존재 → MidLand 미호출")
        void shortLandExists_noMidFallback() {
            stubShortGrids(1);
            stubShortLandBatch(70);

            composer.compose(REGION_ID);

            verify(midLandRepository, never()).findRecentAll(any(), any());
        }

        @Test
        @DisplayName("7일 AM/PM 구조 생성")
        void sevenDays_amPm_structure() {
            stubShortGrids(1);
            stubShortLandBatch(70);

            PopView result = composer.compose(REGION_ID);

            assertThat(result.daily().pops()).hasSize(7);
        }

        @Test
        @DisplayName("AM(09:00) / PM(21:00) POP 값 정확히 매핑")
        void amPm_pop_values() {
            stubShortGrids(1);
            stubShortLandBatchWithAmPm(30, 70);

            PopView result = composer.compose(REGION_ID);
            Daily.Pop day0 = result.daily().get(0);

            assertSoftly(softly -> {
                softly.assertThat(day0.am()).isEqualTo(30);
                softly.assertThat(day0.pm()).isEqualTo(70);
            });
        }

        @Test
        @DisplayName("ShortLand POP null → findRecentPop fallback 호출")
        void shortLandPopNull_fallbackToFindRecentPop() {
            stubShortGrids(1);
            // ShortLand의 pop이 null인 경우
            when(shortLandRepository.findRecentAll(eq(cityRegionCode), any()))
                    .thenAnswer(invocation -> {
                        List<LocalDateTime> times = invocation.getArgument(1);
                        return times.stream().collect(Collectors.toMap(
                                et -> et,
                                et -> new ShortLand(SHORT_LAND_ANNOUNCE_TIME, et, CITY_ID, null, 0, 0)
                        ));
                    });
            when(shortLandRepository.findRecentPop(eq(cityRegionCode), any())).thenReturn(45);

            PopView result = composer.compose(REGION_ID);

            verify(shortLandRepository, atLeastOnce()).findRecentPop(eq(cityRegionCode), any());
            // fallback 값이 적용되었는지 확인 (D+0 AM)
            assertThat(result.daily().get(0).am()).isEqualTo(45);
        }
    }


    @Nested
    @DisplayName("Daily - MidLand fallback 경로")
    class DailyMidFallbackPath {

        @Test
        @DisplayName("ShortLand 빈 결과 → MidLand fallback, 7일 구조 생성")
        void midFallback_sevenDays() {
            stubShortGrids(1);
            stubMidFallbackBatch(80);

            PopView result = composer.compose(REGION_ID);

            assertThat(result.daily().pops()).hasSize(7);
        }

        @Test
        @DisplayName("MidLand POP 값이 Daily AM/PM에 각각 매핑")
        void midFallback_popValues() {
            stubShortGrids(1);
            stubMidFallbackBatchWithAmPm(40, 80);

            PopView result = composer.compose(REGION_ID);

            Daily.Pop day0 = result.daily().get(0);
            assertSoftly(softly -> {
                softly.assertThat(day0.am()).isEqualTo(40);
                softly.assertThat(day0.pm()).isEqualTo(80);
            });
        }

        @Test
        @DisplayName("ProvinceRegionCode 없음 → MidLand 조회 스킵, Daily 전체 null")
        void noProvinceRegionCode_allNull() {
            stubShortGrids(1);
            when(shortLandRepository.findRecentAll(eq(cityRegionCode), any())).thenReturn(Map.of());
            when(provinceRegionCodeRepository.findById(PROVINCE_ID)).thenReturn(Optional.empty());

            PopView result = composer.compose(REGION_ID);

            verify(midLandRepository, never()).findRecentAll(any(), any());
            assertThat(result.daily().pops())
                    .allSatisfy(p -> {
                        assertThat(p.am()).isNull();
                        assertThat(p.pm()).isNull();
                    });
        }
    }


    @Nested
    @DisplayName("Daily - 인덱스 기반 매핑")
    class DailyIndexMapping {

        @Test
        @DisplayName("중간 일자 데이터 누락 시 해당 일자만 null, 인덱스 밀림 없음")
        void missingMiddleDay_noIndexShift() {
            stubShortGrids(1);
            // D+0, D+2만 ShortLand 존재, D+1 누락
            when(shortLandRepository.findRecentAll(eq(cityRegionCode), any()))
                    .thenAnswer(invocation -> {
                        List<LocalDateTime> times = invocation.getArgument(1);
                        Map<LocalDateTime, ShortLand> result = new HashMap<>();
                        for (LocalDateTime et : times) {
                            // D+0 (index 0,1) 과 D+2 (index 4,5)만 포함
                            int idx = times.indexOf(et);
                            if (idx <= 1 || (idx >= 4 && idx <= 5)) {
                                result.put(et, new ShortLand(
                                        SHORT_LAND_ANNOUNCE_TIME, et, CITY_ID, 50, 0, 0));
                            }
                        }
                        return result;
                    });
            // D+1의 missingTimes → MidLand도 없음
            when(provinceRegionCodeRepository.findById(PROVINCE_ID))
                    .thenReturn(Optional.of(provinceRegionCode));
            when(midLandRepository.findRecentAll(eq(provinceRegionCode), any()))
                    .thenReturn(Map.of());

            PopView result = composer.compose(REGION_ID);

            assertSoftly(softly -> {
                // D+0: 데이터 존재
                softly.assertThat(result.daily().get(0).am()).isEqualTo(50);
                softly.assertThat(result.daily().get(0).pm()).isEqualTo(50);
                // D+1: 누락 → null
                softly.assertThat(result.daily().get(1).am()).isNull();
                softly.assertThat(result.daily().get(1).pm()).isNull();
                // D+2: 데이터 존재
                softly.assertThat(result.daily().get(2).am()).isEqualTo(50);
                softly.assertThat(result.daily().get(2).pm()).isEqualTo(50);
            });
        }
    }


    // ======================= helper =======================

    private void stubShortGrids(int count) {
        AnnounceTime at = new AnnounceTime(GRID_ANNOUNCE_TIME);
        List<ShortGrid> grids = IntStream.rangeClosed(1, count)
                .mapToObj(i -> new ShortGrid(
                        at, GRID_ANNOUNCE_TIME.plusHours(i), 60, 127, i * 3, i))
                .toList();
        when(shortGridRepository.findRecentByXAndY(eq(60), eq(127))).thenReturn(grids);
    }

    private void stubShortLandBatch(int pop) {
        when(shortLandRepository.findRecentAll(eq(cityRegionCode), any()))
                .thenAnswer(invocation -> {
                    List<LocalDateTime> times = invocation.getArgument(1);
                    return times.stream().collect(Collectors.toMap(
                            et -> et,
                            et -> new ShortLand(SHORT_LAND_ANNOUNCE_TIME, et, CITY_ID, pop, 0, 0)
                    ));
                });
    }

    private void stubShortLandBatchWithAmPm(int amPop, int pmPop) {
        when(shortLandRepository.findRecentAll(eq(cityRegionCode), any()))
                .thenAnswer(invocation -> {
                    List<LocalDateTime> times = invocation.getArgument(1);
                    return times.stream().collect(Collectors.toMap(
                            et -> et,
                            et -> {
                                boolean isMorning = et.getHour() == 9;
                                int pop = isMorning ? amPop : pmPop;
                                return new ShortLand(SHORT_LAND_ANNOUNCE_TIME, et, CITY_ID, pop, 0, 0);
                            }
                    ));
                });
    }

    private void stubMidFallbackBatch(int pop) {
        when(shortLandRepository.findRecentAll(eq(cityRegionCode), any()))
                .thenReturn(Map.of());

        when(provinceRegionCodeRepository.findById(PROVINCE_ID))
                .thenReturn(Optional.of(provinceRegionCode));

        MidAnnounceTime midAnnounceTime = new MidAnnounceTime(
                LocalDateTime.of(2026, 3, 28, 12, 0));

        when(midLandRepository.findRecentAll(eq(provinceRegionCode), any()))
                .thenAnswer(invocation -> {
                    List<LocalDateTime> times = invocation.getArgument(1);
                    return times.stream().collect(Collectors.toMap(
                            et -> et,
                            et -> new MidLand(midAnnounceTime, et, PROVINCE_ID, pop)
                    ));
                });
    }

    private void stubMidFallbackBatchWithAmPm(int amPop, int pmPop) {
        when(shortLandRepository.findRecentAll(eq(cityRegionCode), any()))
                .thenReturn(Map.of());

        when(provinceRegionCodeRepository.findById(PROVINCE_ID))
                .thenReturn(Optional.of(provinceRegionCode));

        MidAnnounceTime midAnnounceTime = new MidAnnounceTime(
                LocalDateTime.of(2026, 3, 28, 12, 0));

        when(midLandRepository.findRecentAll(eq(provinceRegionCode), any()))
                .thenAnswer(invocation -> {
                    List<LocalDateTime> times = invocation.getArgument(1);
                    return times.stream().collect(Collectors.toMap(
                            et -> et,
                            et -> {
                                int pop = et.getHour() == 9 ? amPop : pmPop;
                                return new MidLand(midAnnounceTime, et, PROVINCE_ID, pop);
                            }
                    ));
                });
    }
}