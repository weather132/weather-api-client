//package com.github.yun531.climate.forecast.infra;
//
//import com.github.yun531.climate.forecast.domain.readmodel.ForecastDailyView;
//import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyView;
//import com.github.yun531.climate.snapshot.contract.SnapshotReader;
//import com.github.yun531.climate.snapshot.contract.WeatherSnapshot;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class SnapshotForecastViewReaderTest {
//
//    @Mock SnapshotReader snapshotReader;
//    @Mock ForecastViewMapper mapper;
//
//    private SnapshotForecastViewReader reader;
//
//    private static final LocalDateTime ANNOUNCE_TIME =
//            LocalDateTime.of(2026, 1, 22, 5, 0);
//
//    @BeforeEach
//    void setUp() {
//        reader = new SnapshotForecastViewReader(snapshotReader, mapper);
//    }
//
//    @Test
//    @DisplayName("loadHourly — snapshot 조회 후 mapper.toHourlyView 위임")
//    void loadHourly_delegatesToMapper() {
//        WeatherSnapshot snap = new WeatherSnapshot("R1", ANNOUNCE_TIME,
//                List.of(), List.of());
//        ForecastHourlyView expected = new ForecastHourlyView("R1", ANNOUNCE_TIME, List.of());
//
//        when(snapshotReader.loadCurrent("R1")).thenReturn(snap);
//        when(mapper.toHourlyView(snap)).thenReturn(expected);
//
//        assertThat(reader.loadHourly("R1")).isSameAs(expected);
//    }
//
//    @Test
//    @DisplayName("loadDaily — snapshot 조회 후 mapper.toDailyView 위임")
//    void loadDaily_delegatesToMapper() {
//        WeatherSnapshot snap = new WeatherSnapshot("R1", ANNOUNCE_TIME,
//                List.of(), List.of());
//        ForecastDailyView expected = new ForecastDailyView("R1", ANNOUNCE_TIME, List.of());
//
//        when(snapshotReader.loadCurrent("R1")).thenReturn(snap);
//        when(mapper.toDailyView(snap)).thenReturn(expected);
//
//        assertThat(reader.loadDaily("R1")).isSameAs(expected);
//    }
//
//    @Test
//    @DisplayName("snapshot null — mapper에 null 전달, 결과 그대로 반환")
//    void snapshotNull_passesNullToMapper() {
//        when(snapshotReader.loadCurrent("R1")).thenReturn(null);
//        when(mapper.toHourlyView(null)).thenReturn(null);
//
//        assertThat(reader.loadHourly("R1")).isNull();
//    }
//}