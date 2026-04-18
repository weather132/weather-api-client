//package com.github.yun531.climate.notification.infra.alert;
//
//import com.github.yun531.climate.notification.domain.readmodel.PopView;
//import com.github.yun531.climate.snapshot.contract.HourlyPoint;
//import com.github.yun531.climate.snapshot.contract.SnapshotReader;
//import com.github.yun531.climate.snapshot.contract.WeatherSnapshot;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class SnapshotPopViewReaderTest {
//
//    @Mock SnapshotReader snapshotReader;
//    @Mock PopViewMapper mapper;
//
//    private SnapshotPopViewReader reader;
//
//    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 1, 22, 5, 0);
//
//    @BeforeEach
//    void setUp() {
//        reader = new SnapshotPopViewReader(snapshotReader, mapper);
//    }
//
//
//    @Nested
//    @DisplayName("loadCurrent / loadPrevious (단건 조회)")
//    class SingleLoad {
//
//        @Test
//        @DisplayName("loadCurrent -> snapshot 조회 후 mapper 위임")
//        void loadCurrent() {
//            WeatherSnapshot snap = buildSnapshot();
//            PopView expected = mock(PopView.class);
//
//            when(snapshotReader.loadCurrent("R1")).thenReturn(snap);
//            when(mapper.toPopView(snap)).thenReturn(expected);
//
//            assertThat(reader.loadCurrent("R1")).isSameAs(expected);
//        }
//
//        @Test
//        @DisplayName("loadPrevious -> snapshot 조회 후 mapper 위임")
//        void loadPrevious() {
//            WeatherSnapshot snap = buildSnapshot();
//            PopView expected = mock(PopView.class);
//
//            when(snapshotReader.loadPrevious("R1")).thenReturn(snap);
//            when(mapper.toPopView(snap)).thenReturn(expected);
//
//            assertThat(reader.loadPrevious("R1")).isSameAs(expected);
//        }
//    }
//
//
//    @Nested
//    @DisplayName("loadCurrentPreviousPair")
//    class LoadPair {
//
//        @Test
//        @DisplayName("둘 다 존재 -> Pair 반환")
//        void bothExist() {
//            WeatherSnapshot curSnap = buildSnapshot();
//            WeatherSnapshot prevSnap = buildSnapshot();
//            PopView curView = mock(PopView.class);
//            PopView prevView = mock(PopView.class);
//            PopView.Pair expected = new PopView.Pair(curView, prevView);
//
//            when(snapshotReader.loadCurrent("R1")).thenReturn(curSnap);
//            when(snapshotReader.loadPrevious("R1")).thenReturn(prevSnap);
//            when(mapper.toPair(curSnap, prevSnap)).thenReturn(expected);
//
//            PopView.Pair result = reader.loadCurrentPreviousPair("R1");
//
//            assertThat(result).isSameAs(expected);
//        }
//
//        @Test
//        @DisplayName("하나라도 null -> null")
//        void oneNull() {
//            when(snapshotReader.loadCurrent("R1")).thenReturn(null);
//            when(snapshotReader.loadPrevious("R1")).thenReturn(buildSnapshot());
//
//            assertThat(reader.loadCurrentPreviousPair("R1")).isNull();
//        }
//    }
//
//    // ==================== helper ====================
//
//    private WeatherSnapshot buildSnapshot() {
//        List<HourlyPoint> hourly = new ArrayList<>(26);
//        for (int i = 0; i < 26; i++) {
//            hourly.add(new HourlyPoint(ANNOUNCE_TIME.plusHours(i + 1), i, i * 3));
//        }
//        return new WeatherSnapshot("R1", ANNOUNCE_TIME, hourly, List.of());
//    }
//}