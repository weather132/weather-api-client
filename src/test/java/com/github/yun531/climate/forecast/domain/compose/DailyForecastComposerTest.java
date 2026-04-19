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
     * ShortLand announceTime(17:00)도 같은 날이므로 baseDate = 03-28로 daysAhead 정합.
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
        @DisplayName("ShortLand 데이터 존재 시 ShortLand 값으로 DailyRawItem 생성, Mid 미호출")
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
    }


    @Nested
    @DisplayName("Mid fallback 경로")
    class MidFallbackPath {

        @Test
        @DisplayName("ShortLand 빈 결과 → Mid fallback, 7일 구조 생성")
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

            assertThat(result.forecastDailyPoints()).hasSize(7);
        }

        @Test
        @DisplayName("Mid fallback 시 announceTime이 MidLand에서 추출")
        void mid_fallback_announceTime() {
            stubMidFallbackBatch(80, 25, 10);

            DailyComposeResult result = composer.compose(regionCode);

            assertThat(result.announceTime()).isNotNull();
        }
    }


    @Nested
    @DisplayName("집계 로직")
    class Aggregation {

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
        @DisplayName("오전/오후 강수확률 및 기온 최저/최고값 추출")
        void am_pm_pop_and_min_max_temp() {
            stubShortLandBatchWithDifferentValues();

            DailyComposeResult result = composer.compose(regionCode);

            // daysAhead 0에 오전(09시) POP=30, temp=15 + 오후(21시) POP=70, temp=20 존재
            ForecastDailyPoint day0 = result.forecastDailyPoints().stream()
                    .filter(p -> p.daysAhead() == 0)
                    .findFirst().orElseThrow();

            assertSoftly(softly -> {
                softly.assertThat(day0.amPop()).isEqualTo(30);
                softly.assertThat(day0.pmPop()).isEqualTo(70);
                softly.assertThat(day0.minTemp()).isEqualTo(15);
                softly.assertThat(day0.maxTemp()).isEqualTo(20);
            });
        }

        @Test
        @DisplayName("데이터 부재 시 null을 포함한 7일 기본 구조 반환")
        void empty_input_returns_default_structure() {
            when(shortLandRepository.findRecentAll(eq(regionCode), any()))
                    .thenReturn(Map.of());
            when(provinceRegionCodeRepository.findById(PROVINCE_ID))
                    .thenReturn(Optional.empty());

            DailyComposeResult result = composer.compose(regionCode);

            assertSoftly(softly -> {
                softly.assertThat(result.forecastDailyPoints()).hasSize(7);
                softly.assertThat(result.forecastDailyPoints()).allSatisfy(p -> {
                    softly.assertThat(p.minTemp()).isNull();
                    softly.assertThat(p.maxTemp()).isNull();
                    softly.assertThat(p.amPop()).isNull();
                    softly.assertThat(p.pmPop()).isNull();
                });
            });
        }

        @Test
        @DisplayName("0~6일 범위를 벗어난 데이터 무시")
        void out_of_range_ignored() {
            // announceTime을 8일 전으로 설정 → 모든 effectiveTime이 daysAhead >= 8 → 범위 밖
            LocalDateTime farPastAnnounceTime = LocalDateTime.of(2026, 3, 20, 17, 0);
            when(shortLandRepository.findRecentAll(eq(regionCode), any()))
                    .thenAnswer(invocation -> {
                        List<LocalDateTime> times = invocation.getArgument(1);
                        return times.stream().collect(Collectors.toMap(
                                et -> et,
                                et -> new ShortLand(farPastAnnounceTime, et, CITY_ID, 50, 10, 0)
                        ));
                    });

            DailyComposeResult result = composer.compose(regionCode);

            assertSoftly(softly -> {
                softly.assertThat(result.forecastDailyPoints()).hasSize(7);
                softly.assertThat(result.forecastDailyPoints()).allSatisfy(p -> {
                    softly.assertThat(p.minTemp()).isNull();
                    softly.assertThat(p.amPop()).isNull();
                });
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