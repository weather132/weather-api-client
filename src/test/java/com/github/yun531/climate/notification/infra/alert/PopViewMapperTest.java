//package com.github.yun531.climate.notification.infra.alert;
//
//import com.github.yun531.climate.notification.domain.readmodel.PopView;
//import com.github.yun531.climate.snapshot.contract.DailyPoint;
//import com.github.yun531.climate.snapshot.contract.HourlyPoint;
//import com.github.yun531.climate.snapshot.contract.WeatherSnapshot;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class PopViewMapperTest {
//
//    private final PopViewMapper mapper = new PopViewMapper();
//
//    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 1, 22, 5, 0);
//
//
//    @Nested
//    @DisplayName("toPopView")
//    class ToPopView {
//
//        @Test
//        @DisplayName("정상 스냅샷 -> hourly 26개, daily 7개, announceTime 매핑")
//        void normalSnapshot() {
//            WeatherSnapshot snap = buildSnapshot(26, 7);
//
//            PopView view = mapper.toPopView(snap);
//
//            assertThat(view.announceTime()).isEqualTo(ANNOUNCE_TIME);
//            assertThat(view.hourly().pops()).hasSize(26);
//            assertThat(view.daily().pops()).hasSize(7);
//        }
//
//        @Test
//        @DisplayName("hourly 26개 미만 -> null 패딩")
//        void shortHourly_padded() {
//            WeatherSnapshot snap = buildSnapshot(10, 7);
//
//            PopView view = mapper.toPopView(snap);
//
//            assertThat(view.hourly().pops()).hasSize(26);
//            assertThat(view.hourly().pops().get(10).effectiveTime()).isNull();
//            assertThat(view.hourly().pops().get(10).pop()).isNull();
//        }
//
//        @Test
//        @DisplayName("null 스냅샷 -> null 반환")
//        void nullSnapshot() {
//            assertThat(mapper.toPopView(null)).isNull();
//        }
//
//        @Test
//        @DisplayName("데이터 있으면 am/pm 값 유지, 없으면 null")
//        void valuesPreserved_nullsPassedThrough() {
//            List<DailyPoint> daily = new ArrayList<>(7);
//            daily.add(new DailyPoint(0, -5, 5, 30, 60));
//            for (int d = 1; d < 7; d++) {
//                daily.add(new DailyPoint(d, null, null, null, null));
//            }
//            WeatherSnapshot snap = new WeatherSnapshot("R1", ANNOUNCE_TIME,
//                    buildHourlyPoints(26), daily);
//
//            PopView view = mapper.toPopView(snap);
//
//            assertThat(view.daily().pops().get(0).am()).isEqualTo(30);
//            assertThat(view.daily().pops().get(0).pm()).isEqualTo(60);
//            assertThat(view.daily().pops().get(1).am()).isNull();
//            assertThat(view.daily().pops().get(1).pm()).isNull();
//        }
//
//        @Test
//        @DisplayName("빈 리스트 -> 7개 null 패딩")
//        void emptyDaily_padded() {
//            WeatherSnapshot snap = new WeatherSnapshot("R1", ANNOUNCE_TIME,
//                    buildHourlyPoints(26), List.of());
//
//            PopView view = mapper.toPopView(snap);
//
//            assertThat(view.daily().pops()).hasSize(7);
//            assertThat(view.daily().pops().get(0).am()).isNull();
//        }
//    }
//
//
//    @Nested
//    @DisplayName("toPair")
//    class ToPair {
//
//        @Test
//        @DisplayName("둘 다 정상 -> Pair 반환")
//        void bothPresent() {
//            WeatherSnapshot snap = buildSnapshot(26, 7);
//
//            assertThat(mapper.toPair(snap, snap)).isNotNull();
//        }
//
//        @Test
//        @DisplayName("하나라도 null -> null 반환")
//        void oneNull() {
//            WeatherSnapshot snap = buildSnapshot(26, 7);
//
//            assertThat(mapper.toPair(null, snap)).isNull();
//            assertThat(mapper.toPair(snap, null)).isNull();
//        }
//    }
//
//    // ==================== helper ====================
//
//    private WeatherSnapshot buildSnapshot(int hourlyCount, int dailyCount) {
//        List<DailyPoint> daily = new ArrayList<>(dailyCount);
//        for (int d = 0; d < dailyCount; d++) {
//            daily.add(new DailyPoint(d, -d, d + 10, d * 10, d * 10 + 5));
//        }
//        return new WeatherSnapshot("R1", ANNOUNCE_TIME, buildHourlyPoints(hourlyCount), daily);
//    }
//
//    private List<HourlyPoint> buildHourlyPoints(int count) {
//        List<HourlyPoint> hourly = new ArrayList<>(count);
//        for (int i = 0; i < count; i++) {
//            hourly.add(new HourlyPoint(ANNOUNCE_TIME.plusHours(i + 1), i * 2, i * 3));
//        }
//        return hourly;
//    }
//}