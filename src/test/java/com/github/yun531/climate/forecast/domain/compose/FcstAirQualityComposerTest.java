package com.github.yun531.climate.forecast.domain.compose;

import com.github.yun531.climate.airQuality.domain.AirQuality;
import com.github.yun531.climate.airQuality.domain.AirQualityRepository;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.forecast.domain.readmodel.AirQualityGradeThresholds;
import com.github.yun531.climate.forecast.domain.readmodel.AirQualityView;
import com.github.yun531.climate.forecast.domain.readmodel.RegionAirQualityView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FcstAirQualityComposer")
class FcstAirQualityComposerTest {

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 5, 21, 10, 0);

    // PM10: good=30, moderate=80, bad=150 / PM25: good=15, moderate=35, bad=75
    private final AirQualityGradeThresholds thresholds = new AirQualityGradeThresholds(
            new AirQualityGradeThresholds.Thresholds(30, 80, 150),
            new AirQualityGradeThresholds.Thresholds(15, 35, 75));

    @Mock CityRegionCodeRepository cityRegionCodeRepository;
    @Mock AirQualityRepository airQualityRepository;

    private FcstAirQualityComposer composer;

    @BeforeEach
    void setUp() {
        composer = new FcstAirQualityComposer(
                cityRegionCodeRepository, airQualityRepository, thresholds);
    }

    @Nested
    @DisplayName("정상 조립")
    class Compose {

        @Test
        @DisplayName("최신 측정값 -> 등급 평가 포함 view (시간 제한 없음)")
        void composesWithGrade() {
            CityRegionCode city = cityWithSido(10L);
            when(cityRegionCodeRepository.findByRegionCode("R1")).thenReturn(city);
            when(airQualityRepository.findLatestBySido(eq(10L)))
                    .thenReturn(Optional.of(new AirQuality(10L, ANNOUNCE_TIME, 95, 80)));

            AirQualityView view = composer.compose("R1");

            assertThat(view.announceTime()).isEqualTo(ANNOUNCE_TIME);
            assertThat(view.pm10()).isEqualTo(95);
            assertThat(view.pm10Grade()).isEqualTo("BAD");       // 80 < 95 <= 150
            assertThat(view.pm25()).isEqualTo(80);
            assertThat(view.pm25Grade()).isEqualTo("VERY_BAD");  // 80 > 75
        }
    }

    @Nested
    @DisplayName("빈 view")
    class Empty {

        @Test
        @DisplayName("미존재 regionId -> 빈 view")
        void unknownRegion() {
            when(cityRegionCodeRepository.findByRegionCode("R1")).thenReturn(null);

            AirQualityView view = composer.compose("R1");

            assertThat(view.announceTime()).isNull();
            assertThat(view.pm10()).isNull();
            assertThat(view.pm25()).isNull();
        }

        @Test
        @DisplayName("측정 자체가 없음 -> 빈 view")
        void noMeasurement() {
            CityRegionCode city = cityWithSido(10L);
            when(cityRegionCodeRepository.findByRegionCode("R1")).thenReturn(city);
            when(airQualityRepository.findLatestBySido(eq(10L)))
                    .thenReturn(Optional.empty());

            AirQualityView view = composer.compose("R1");

            assertThat(view.announceTime()).isNull();
            assertThat(view.pm10()).isNull();
        }
    }

    @Nested
    @DisplayName("composeAll - 다지역")
    class ComposeAll {

        @Test
        @DisplayName("각 regionId 의 view 를 등급 포함해 반환")
        void composesEachWithGrade() {
            stubRegionSido("R1", 10L);
            stubRegionSido("R2", 20L);
            stubLatest(10L, new AirQuality(10L, ANNOUNCE_TIME, 40, 20));
            stubLatest(20L, new AirQuality(20L, ANNOUNCE_TIME, 95, 80));

            List<RegionAirQualityView> result = composer.composeAll(List.of("R1", "R2"));

            assertThat(result).extracting(r -> r.view().pm10()).containsExactly(40, 95);
            assertThat(result).extracting(r -> r.view().pm10Grade())
                    .containsExactly("MODERATE", "BAD");
        }

        @Test
        @DisplayName("응답은 요청 regionId 순서를 보존")
        void preservesRequestOrder() {
            stubRegionSido("R3", 30L);
            stubRegionSido("R1", 10L);
            stubRegionSido("R2", 20L);
            stubLatest(30L, new AirQuality(30L, ANNOUNCE_TIME, 10, 5));
            stubLatest(10L, new AirQuality(10L, ANNOUNCE_TIME, 40, 20));
            stubLatest(20L, new AirQuality(20L, ANNOUNCE_TIME, 95, 80));

            List<RegionAirQualityView> result = composer.composeAll(List.of("R3", "R1", "R2"));

            assertThat(result).extracting(RegionAirQualityView::regionId)
                    .containsExactly("R3", "R1", "R2");
        }

        @Test
        @DisplayName("미존재 regionId -> 빈 view 로 포함, 순서 유지")
        void unknownRegion_emptyViewIncluded() {
            stubRegionSido("R1", 10L);
            when(cityRegionCodeRepository.findByRegionCode("RX")).thenReturn(null);
            stubLatest(10L, new AirQuality(10L, ANNOUNCE_TIME, 40, 20));

            List<RegionAirQualityView> result = composer.composeAll(List.of("R1", "RX"));

            assertThat(result).hasSize(2);
            assertThat(result.get(1).regionId()).isEqualTo("RX");
            assertThat(result.get(1).view().pm10()).isNull();
        }
    }

    private CityRegionCode cityWithSido(Long sidoId) {
        CityRegionCode city = mock(CityRegionCode.class);
        when(city.getSidoRegionCodeId()).thenReturn(sidoId);
        return city;
    }

    private void stubRegionSido(String regionId, Long sidoId) {
        CityRegionCode city = mock(CityRegionCode.class);
        when(city.getSidoRegionCodeId()).thenReturn(sidoId);
        when(cityRegionCodeRepository.findByRegionCode(regionId)).thenReturn(city);
    }

    private void stubLatest(Long sidoId, AirQuality measurement) {
        when(airQualityRepository.findLatestBySido(eq(sidoId)))
                .thenReturn(Optional.of(measurement));
    }
}