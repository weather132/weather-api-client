//package com.github.yun531.climate.forecast.infra;
//
//import com.github.yun531.climate.forecast.domain.readmodel.ForecastDailyView;
//import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyView;
//import com.github.yun531.climate.snapshot.contract.DailyPoint;
//import com.github.yun531.climate.snapshot.contract.HourlyPoint;
//import com.github.yun531.climate.snapshot.contract.WeatherSnapshot;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class ForecastViewMapperTest {
//
//    private final ForecastViewMapper mapper = new ForecastViewMapper();
//
//    private static final LocalDateTime ANNOUNCE_TIME =
//            LocalDateTime.of(2026, 1, 22, 5, 0);
//
//    @Nested
//    @DisplayName("toHourlyView")
//    class ToHourlyView {
//
//        @Test
//        @DisplayName("effectiveTime 정렬 + 필드 매핑")
//        void sortsAndMaps() {
//            WeatherSnapshot snap = new WeatherSnapshot("R1", ANNOUNCE_TIME, List.of(
//                    new HourlyPoint(ANNOUNCE_TIME.plusHours(2), 5, 40),
//                    new HourlyPoint(ANNOUNCE_TIME.plusHours(1), 2, 20)
//            ), List.of());
//
//            ForecastHourlyView view = mapper.toHourlyView(snap);
//
//            assertThat(view.regionId()).isEqualTo("R1");
//            assertThat(view.announceTime()).isEqualTo(ANNOUNCE_TIME);
//            assertThat(view.hourlyPoints()).hasSize(2);
//            assertThat(view.hourlyPoints().get(0).effectiveTime()).isEqualTo(ANNOUNCE_TIME.plusHours(1));
//            assertThat(view.hourlyPoints().get(0).temp()).isEqualTo(2);
//            assertThat(view.hourlyPoints().get(0).pop()).isEqualTo(20);
//        }
//
//        @Test
//        @DisplayName("빈 hourly -> 빈 포인트 리스트")
//        void emptyHourly() {
//            WeatherSnapshot snap = new WeatherSnapshot("R1", ANNOUNCE_TIME, List.of(), List.of());
//
//            assertThat(mapper.toHourlyView(snap).hourlyPoints()).isEmpty();
//        }
//    }
//
//    @Nested
//    @DisplayName("toDailyView")
//    class ToDailyView {
//
//        @Test
//        @DisplayName("daysAhead 정렬 + 필드 매핑")
//        void sortsAndMaps() {
//            WeatherSnapshot snap = new WeatherSnapshot("R1", ANNOUNCE_TIME, List.of(),
//                    List.of(
//                            new DailyPoint(1, -3, 7, 20, 40),
//                            new DailyPoint(0, -5, 5, 10, 25)
//                    ));
//
//            ForecastDailyView view = mapper.toDailyView(snap);
//
//            assertThat(view.dailyPoints()).hasSize(2);
//            assertThat(view.dailyPoints().get(0).daysAhead()).isZero();
//            assertThat(view.dailyPoints().get(0).minTemp()).isEqualTo(-5);
//            assertThat(view.dailyPoints().get(0).maxTemp()).isEqualTo(5);
//            assertThat(view.dailyPoints().get(0).amPop()).isEqualTo(10);
//            assertThat(view.dailyPoints().get(0).pmPop()).isEqualTo(25);
//        }
//
//        @Test
//        @DisplayName("빈 daily -> 빈 포인트 리스트")
//        void emptyDaily() {
//            WeatherSnapshot snap = new WeatherSnapshot("R1", ANNOUNCE_TIME, List.of(), List.of());
//
//            assertThat(mapper.toDailyView(snap).dailyPoints()).isEmpty();
//        }
//    }
//
//    @Nested
//    @DisplayName("공통")
//    class Common {
//
//        @Test
//        @DisplayName("null 스냅샷 -> null 반환")
//        void nullSnapshot_returnsNull() {
//            assertThat(mapper.toHourlyView(null)).isNull();
//            assertThat(mapper.toDailyView(null)).isNull();
//        }
//    }
//}