//package com.github.yun531.climate.snapshot.domain.compose;
//
//import com.github.yun531.climate.snapshot.contract.DailyPoint;
//import com.github.yun531.climate.snapshot.contract.HourlyPoint;
//import com.github.yun531.climate.snapshot.contract.WeatherSnapshot;
//import com.github.yun531.climate.snapshot.domain.model.DailyForecastItem;
//import com.github.yun531.climate.snapshot.domain.model.HourlyForecastItem;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.IntStream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.SoftAssertions.assertSoftly;
//
//@DisplayName("SnapshotAssembler 통합 테스트")
//class SnapshotAssemblerTest {
//
//    private final SnapshotAssembler assembler = new SnapshotAssembler();
//
//    private static final String REGION_ID            = "11B10101";
//    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 3, 28, 14, 0);
//
//    @Nested
//    @DisplayName("시간별 예보(HourlyPoints) 변환")
//    class ToHourlyPoints {
//
//        @Test
//        @DisplayName("26개 항목의 POP(강수확률) 데이터가 순서대로 보존")
//        void popPattern_preserved() {
//            Integer[] pops = {
//                    0, 0, 30, 70, 80, 70, 30, 0, 0, 0,
//                    0, 0, 70, 80, 70, 0, 0, 60, 70, 80,
//                    60, 0, 0, 0, 0, 0
//            };
//            List<HourlyForecastItem> hourlyItems = createHourlyItems(pops);
//
//            WeatherSnapshot snap = assembler.assemble(REGION_ID, ANNOUNCE_TIME, hourlyItems, List.of());
//
//            assertThat(snap.hourly()).hasSize(26);
//            assertThat(snap.hourly()).extracting(HourlyPoint::pop)
//                    .containsExactly(pops);
//        }
//
//        @Test
//        @DisplayName("null 제외, 시간순(effectiveTime) 정렬, 최대 26개 지점 제한 로직")
//        void combined_refinement_logic() {
//            List<HourlyForecastItem> mixedItems = new ArrayList<>();
//            mixedItems.add(new HourlyForecastItem(null, 50, 10));
//            for (int i = 1; i <= 30; i++) {
//                mixedItems.add(new HourlyForecastItem(ANNOUNCE_TIME.plusHours(i), 10, i));
//            }
//            Collections.shuffle(mixedItems);
//
//            WeatherSnapshot snap = assembler.assemble(REGION_ID, ANNOUNCE_TIME, mixedItems, List.of());
//
//            assertSoftly(softly -> {
//                softly.assertThat(snap.hourly()).hasSize(26);
//                softly.assertThat(snap.hourly()).extracting(HourlyPoint::effectiveTime).isSorted();
//                softly.assertThat(snap.hourly()).allMatch(p -> p.effectiveTime() != null);
//                softly.assertThat(snap.hourly().get(0).effectiveTime()).isEqualTo(ANNOUNCE_TIME.plusHours(1));
//            });
//        }
//    }
//
//    @Nested
//    @DisplayName("일별 예보(DailyPoints) 변환")
//    class ToDailyPoints {
//
//        @Test
//        @DisplayName("당일 포함 7일치(daysAhead 0~6) 데이터의 생성 및 검증")
//        void seven_daily_points_structure() {
//            WeatherSnapshot snap = assembler.assemble(REGION_ID, ANNOUNCE_TIME, List.of(), List.of());
//
//            assertThat(snap.daily()).hasSize(7);
//            assertThat(snap.daily()).extracting(DailyPoint::daysAhead)
//                    .containsExactly(0, 1, 2, 3, 4, 5, 6);
//        }
//
//        @Test
//        @DisplayName("오전*오후 강수확률 및 기온 최저*최고값 추출")
//        void aggregation_logic() {
//            LocalDateTime day1 = ANNOUNCE_TIME.plusDays(1);
//            List<DailyForecastItem> items = List.of(
//                    new DailyForecastItem(ANNOUNCE_TIME, day1.withHour(9), 15, 30),
//                    new DailyForecastItem(ANNOUNCE_TIME, day1.withHour(21), 20, 70)
//            );
//
//            WeatherSnapshot snap = assembler.assemble(REGION_ID, ANNOUNCE_TIME, List.of(), items);
//
//            DailyPoint target = snap.daily().get(1);
//
//            assertSoftly(softly -> {
//                softly.assertThat(target.amPop()).isEqualTo(30);
//                softly.assertThat(target.pmPop()).isEqualTo(70);
//                softly.assertThat(target.minTemp()).isEqualTo(15);
//                softly.assertThat(target.maxTemp()).isEqualTo(20);
//            });
//        }
//
//        @Test
//        @DisplayName("0~6일 범위를 벗어난 데이터 무시")
//        void out_of_range_ignored() {
//            List<DailyForecastItem> items = List.of(
//                    new DailyForecastItem(ANNOUNCE_TIME, ANNOUNCE_TIME.minusDays(1), 10, 50),
//                    new DailyForecastItem(ANNOUNCE_TIME, ANNOUNCE_TIME.plusDays(7), 20, 80)
//            );
//
//            WeatherSnapshot snap = assembler.assemble(REGION_ID, ANNOUNCE_TIME, List.of(), items);
//
//            assertThat(snap.daily()).allSatisfy(d -> {
//                assertThat(d.minTemp()).isNull();
//                assertThat(d.amPop()).isNull();
//            });
//        }
//
//        @Test
//        @DisplayName("데이터 부재 시 null을 포함한 7일 기본 구조 반환")
//        void empty_input_returns_default_structure() {
//            WeatherSnapshot snap = assembler.assemble(REGION_ID, ANNOUNCE_TIME, List.of(), List.of());
//
//            assertSoftly(softly -> {
//                softly.assertThat(snap.daily()).hasSize(7);
//                softly.assertThat(snap.daily()).allSatisfy(d -> {
//                    softly.assertThat(d.minTemp()).isNull();
//                    softly.assertThat(d.amPop()).isNull();
//                });
//            });
//        }
//    }
//
//    // ==================== helper ====================
//
//    private static List<HourlyForecastItem> createHourlyItems(Integer... pops) {
//        return IntStream.range(0, pops.length)
//                .mapToObj(i -> new HourlyForecastItem(ANNOUNCE_TIME.plusHours(i + 1), pops[i], i + 1))
//                .toList();
//    }
//}