package com.github.yun531.climate.forecast.domain.compose;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.forecast.domain.compose.DailyForecastComposer.DailyComposeResult;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastDailyPoint;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandRepository;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureRepository;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCodeRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyForecastComposerTest {

    @Mock ShortLandRepository shortLandRepository;
    @Mock MidLandRepository midLandRepository;
    @Mock MidTemperatureRepository midTemperatureRepository;
    @Mock ProvinceRegionCodeRepository provinceRegionCodeRepository;
    @Mock CityRegionCode regionCode;
    @Mock ProvinceRegionCode provinceRegionCode;

    private DailyForecastComposer composer;

    private static final Long CITY_ID     = 1L;
    private static final Long PROVINCE_ID = 100L;

    /**
     * 고정 시각: 2026-03-28 14:00.
     * getEffectiveTimes()가 이 시각 기준으로 D+0(03-28 09:00/21:00) ~ D+6 시간대를 생성.
     */
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 28, 14, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            NOW.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    private static final LocalDateTime SHORT_LAND_ANNOUNCE_TIME =
            LocalDateTime.of(2026, 3, 28, 17, 0);

    @BeforeEach
    void setUp() {
        composer = new DailyForecastComposer(
                shortLandRepository, midLandRepository,
                midTemperatureRepository, provinceRegionCodeRepository,
                FIXED_CLOCK);

        lenient().when(regionCode.getId()).thenReturn(CITY_ID);
        lenient().when(regionCode.getProvinceRegionCodeId()).thenReturn(PROVINCE_ID);
    }

    @Nested
    @DisplayName("ShortLand 경로")
    class ShortLandPath {

        @Test
        @DisplayName("ShortLand 데이터 존재 시 Mid 미호출")
        void shortLand_path_no_mid_fallback() {
            stubShortLandBatch(70, 22);

            DailyComposeResult result = composer.compose(regionCode);

            assertThat(result.forecastDailyPoints()).hasSize(7);
            verify(midLandRepository, never()).findRecentAll(any(), any());
            verify(midTemperatureRepository, never()).findRecentAll(any(), any());
        }

        @Test
        @DisplayName("ShortLand announceTime이 DailyComposeResult에 전달")
        void shortLand_announceTime_propagated() {
            stubShortLandBatch(70, 22);

            DailyComposeResult result = composer.compose(regionCode);

            assertThat(result.announceTime()).isEqualTo(SHORT_LAND_ANNOUNCE_TIME);
        }

        @Test
        @DisplayName("ShortLand POP null → findRecentPop fallback 호출")
        void shortLandPopNull_fallbackToFindRecentPop() {
            when(shortLandRepository.findRecentAll(eq(regionCode), any()))
                    .thenAnswer(invocation -> {
                        List<LocalDateTime> times = invocation.getArgument(1);
                        return times.stream().collect(Collectors.toMap(
                                et -> et,
                                et -> new ShortLand(SHORT_LAND_ANNOUNCE_TIME, et, CITY_ID, null, 15, 0)
                        ));
                    });
            when(shortLandRepository.findRecentPop(eq(regionCode), any())).thenReturn(45);

            DailyComposeResult result = composer.compose(regionCode);

            verify(shortLandRepository, atLeastOnce()).findRecentPop(eq(regionCode), any());
            // D+0 AM POP이 fallback 값으로 채워짐
            assertThat(result.forecastDailyPoints().get(0).amPop()).isEqualTo(45);
        }

        @Test
        @DisplayName("ShortLand temp null → isMorning에 따라 Min/Max findRecent fallback")
        void shortLandTempNull_fallbackToMinOrMaxTemp() {
            when(shortLandRepository.findRecentAll(eq(regionCode), any()))
                    .thenAnswer(invocation -> {
                        List<LocalDateTime> times = invocation.getArgument(1);
                        return times.stream().collect(Collectors.toMap(
                                et -> et,
                                et -> new ShortLand(SHORT_LAND_ANNOUNCE_TIME, et, CITY_ID, 50, null, 0)
                        ));
                    });
            when(shortLandRepository.findRecentMinTemp(eq(regionCode), any())).thenReturn(10);
            when(shortLandRepository.findRecentMaxTemp(eq(regionCode), any())).thenReturn(25);

            DailyComposeResult result = composer.compose(regionCode);

            // AM(09:00) → minTemp, PM(21:00) → maxTemp
            ForecastDailyPoint day0 = result.forecastDailyPoints().get(0);
            assertSoftly(softly -> {
                softly.assertThat(day0.minTemp()).isEqualTo(10);
                softly.assertThat(day0.maxTemp()).isEqualTo(25);
            });
        }
    }


    @Nested
    @DisplayName("Mid fallback 경로")
    class MidFallbackPath {

        @Test
        @DisplayName("ShortLand 빈 결과 → Mid fallback으로 7일 구조 생성")
        void mid_fallback_pop() {
            stubMidFallbackBatch(80, 25, 10);

            DailyComposeResult result = composer.compose(regionCode);

            assertThat(result.forecastDailyPoints()).hasSize(7);
        }

        @Test
        @DisplayName("Mid fallback 시 오전은 minTemp, 오후는 maxTemp 매핑")
        void mid_fallback_temp_by_time_of_day() {
            stubMidFallbackBatch(80, 25, 10);

            DailyComposeResult result = composer.compose(regionCode);

            // AM 슬롯(day*2) = minTemp, PM 슬롯(day*2+1) = maxTemp
            ForecastDailyPoint day0 = result.forecastDailyPoints().get(0);
            assertSoftly(softly -> {
                softly.assertThat(day0.minTemp()).isEqualTo(10);
                softly.assertThat(day0.maxTemp()).isEqualTo(25);
            });
        }

        @Test
        @DisplayName("Mid fallback 시 announceTime이 MidLand에서 추출")
        void mid_fallback_announceTime() {
            stubMidFallbackBatch(80, 25, 10);

            DailyComposeResult result = composer.compose(regionCode);

            assertThat(result.announceTime()).isNotNull();
        }

        @Test
        @DisplayName("ProvinceRegionCode 없음 → MidLand/MidTemp 조회 스킵, 빈 리스트 반환")
        void noProvinceRegionCode_emptyResult() {
            when(shortLandRepository.findRecentAll(eq(regionCode), any())).thenReturn(Map.of());
            when(provinceRegionCodeRepository.findById(PROVINCE_ID)).thenReturn(Optional.empty());

            DailyComposeResult result = composer.compose(regionCode);

            verify(midLandRepository, never()).findRecentAll(any(), any());
            verify(midTemperatureRepository, never()).findRecentAll(any(), any());
            assertThat(result.forecastDailyPoints()).isEmpty();
        }
    }


    @Nested
    @DisplayName("조립 로직")
    class Assembly {

        @Test
        @DisplayName("7일치(daysAhead 0~6) 구조 생성")
        void seven_daily_points_structure() {
            stubShortLandBatch(70, 22);

            DailyComposeResult result = composer.compose(regionCode);

            assertThat(result.forecastDailyPoints()).hasSize(7);
            assertThat(result.forecastDailyPoints())
                    .extracting(ForecastDailyPoint::daysAhead)
                    .containsExactly(0, 1, 2, 3, 4, 5, 6);
        }

        @Test
        @DisplayName("AM(09:00)은 minTemp/amPop, PM(21:00)은 maxTemp/pmPop에 매핑")
        void am_pm_index_based_mapping() {
            stubShortLandBatchWithDifferentValues();

            DailyComposeResult result = composer.compose(regionCode);

            ForecastDailyPoint day0 = result.forecastDailyPoints().get(0);
            assertSoftly(softly -> {
                softly.assertThat(day0.amPop()).isEqualTo(30);
                softly.assertThat(day0.pmPop()).isEqualTo(70);
                softly.assertThat(day0.minTemp()).isEqualTo(15);
                softly.assertThat(day0.maxTemp()).isEqualTo(20);
            });
        }

        @Test
        @DisplayName("데이터 전무 → 빈 리스트 반환 (소비자 isEmpty 가드용)")
        void empty_input_returns_empty_list() {
            when(shortLandRepository.findRecentAll(eq(regionCode), any())).thenReturn(Map.of());
            when(provinceRegionCodeRepository.findById(PROVINCE_ID))
                    .thenReturn(Optional.of(provinceRegionCode));
            when(midTemperatureRepository.findRecentAll(eq(regionCode), any())).thenReturn(Map.of());
            when(midLandRepository.findRecentAll(eq(provinceRegionCode), any())).thenReturn(Map.of());

            DailyComposeResult result = composer.compose(regionCode);

            assertThat(result.forecastDailyPoints()).isEmpty();
        }

        @Test
        @DisplayName("중간 일자 데이터 누락 시 해당 일자만 null, 인덱스 밀림 없음")
        void missing_middle_day_no_index_shift() {
            // D+0, D+2만 ShortLand 존재, D+1 누락
            when(shortLandRepository.findRecentAll(eq(regionCode), any()))
                    .thenAnswer(invocation -> {
                        List<LocalDateTime> times = invocation.getArgument(1);
                        Map<LocalDateTime, ShortLand> result = new HashMap<>();
                        for (int i = 0; i < times.size(); i++) {
                            // D+0 (index 0,1), D+2 (index 4,5)만 포함
                            if (i <= 1 || (i >= 4 && i <= 5)) {
                                LocalDateTime et = times.get(i);
                                result.put(et, new ShortLand(
                                        SHORT_LAND_ANNOUNCE_TIME, et, CITY_ID, 50, 20, 0));
                            }
                        }
                        return result;
                    });
            // D+1 fallback → Mid도 없음
            when(provinceRegionCodeRepository.findById(PROVINCE_ID))
                    .thenReturn(Optional.of(provinceRegionCode));
            when(midTemperatureRepository.findRecentAll(eq(regionCode), any())).thenReturn(Map.of());
            when(midLandRepository.findRecentAll(eq(provinceRegionCode), any())).thenReturn(Map.of());

            DailyComposeResult result = composer.compose(regionCode);

            assertSoftly(softly -> {
                // D+0: 데이터 존재
                softly.assertThat(result.forecastDailyPoints().get(0).amPop()).isEqualTo(50);
                softly.assertThat(result.forecastDailyPoints().get(0).pmPop()).isEqualTo(50);
                // D+1: 누락 → null
                softly.assertThat(result.forecastDailyPoints().get(1).amPop()).isNull();
                softly.assertThat(result.forecastDailyPoints().get(1).pmPop()).isNull();
                softly.assertThat(result.forecastDailyPoints().get(1).minTemp()).isNull();
                softly.assertThat(result.forecastDailyPoints().get(1).maxTemp()).isNull();
                // D+2: 데이터 존재
                softly.assertThat(result.forecastDailyPoints().get(2).amPop()).isEqualTo(50);
                softly.assertThat(result.forecastDailyPoints().get(2).pmPop()).isEqualTo(50);
            });
        }
    }


    // ==================== helper ====================

    private void stubShortLandBatch(int pop, int temp) {
        when(shortLandRepository.findRecentAll(eq(regionCode), any()))
                .thenAnswer(invocation -> {
                    List<LocalDateTime> times = invocation.getArgument(1);
                    return times.stream().collect(Collectors.toMap(
                            et -> et,
                            et -> new ShortLand(SHORT_LAND_ANNOUNCE_TIME, et, CITY_ID, pop, temp, 0)
                    ));
                });
    }

    private void stubShortLandBatchWithDifferentValues() {
        when(shortLandRepository.findRecentAll(eq(regionCode), any()))
                .thenAnswer(invocation -> {
                    List<LocalDateTime> times = invocation.getArgument(1);
                    return times.stream().collect(Collectors.toMap(
                            et -> et,
                            et -> {
                                boolean isMorning = et.getHour() == 9;
                                int pop = isMorning ? 30 : 70;
                                int temp = isMorning ? 15 : 20;
                                return new ShortLand(SHORT_LAND_ANNOUNCE_TIME, et, CITY_ID, pop, temp, 0);
                            }
                    ));
                });
    }

    private void stubMidFallbackBatch(int pop, int maxTemp, int minTemp) {
        when(shortLandRepository.findRecentAll(eq(regionCode), any()))
                .thenReturn(Map.of());

        when(provinceRegionCodeRepository.findById(PROVINCE_ID))
                .thenReturn(Optional.of(provinceRegionCode));

        MidAnnounceTime midAnnounceTime = new MidAnnounceTime(
                LocalDateTime.of(2026, 3, 28, 12, 0));

        when(midTemperatureRepository.findRecentAll(eq(regionCode), any()))
                .thenAnswer(invocation -> {
                    List<LocalDateTime> times = invocation.getArgument(1);
                    return times.stream().collect(Collectors.toMap(
                            et -> et,
                            et -> new MidTemperature(midAnnounceTime, et, CITY_ID, maxTemp, minTemp)
                    ));
                });

        when(midLandRepository.findRecentAll(eq(provinceRegionCode), any()))
                .thenAnswer(invocation -> {
                    List<LocalDateTime> times = invocation.getArgument(1);
                    return times.stream().collect(Collectors.toMap(
                            et -> et,
                            et -> new MidLand(midAnnounceTime, et, PROVINCE_ID, pop)
                    ));
                });
    }
}