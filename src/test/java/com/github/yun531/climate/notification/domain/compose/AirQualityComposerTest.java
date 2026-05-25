package com.github.yun531.climate.notification.domain.compose;

import com.github.yun531.climate.airQuality.domain.AirQuality;
import com.github.yun531.climate.airQuality.infra.persistence.JpaAirQualityRepository;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.notification.domain.readmodel.AirQualityView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AirQualityComposer")
class AirQualityComposerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 21, 11, 30);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            NOW.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @Mock CityRegionCodeRepository cityRegionCodeRepository;
    @Mock JpaAirQualityRepository jpaAirQualityRepository;

    private AirQualityComposer composer;

    @BeforeEach
    void setUp() {
        composer = new AirQualityComposer(
                cityRegionCodeRepository, jpaAirQualityRepository, FIXED_CLOCK);
    }

    @Nested
    @DisplayName("정상 조립")
    class Compose {

        @Test
        @DisplayName("측정값 -> 등급 없는 원시 수치 view")
        void composesRawValues() {
            CityRegionCode city = cityWithSido(10L);
            when(cityRegionCodeRepository.findByRegionCode("R1")).thenReturn(city);
            when(jpaAirQualityRepository.findRecentBySido(eq(10L), any(), any(), any(Limit.class)))
                    .thenReturn(Optional.of(new AirQuality(10L, NOW.minusHours(1), 95, 80)));

            AirQualityView view = composer.compose("R1");

            assertThat(view.announceTime()).isEqualTo(NOW.minusHours(1));
            assertThat(view.pm10()).isEqualTo(95);
            assertThat(view.pm25()).isEqualTo(80);
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
        @DisplayName("3시간 내 측정 없음 -> 빈 view")
        void noRecentMeasurement() {
            CityRegionCode city = cityWithSido(10L);
            when(cityRegionCodeRepository.findByRegionCode("R1")).thenReturn(city);
            when(jpaAirQualityRepository.findRecentBySido(eq(10L), any(), any(), any(Limit.class)))
                    .thenReturn(Optional.empty());

            AirQualityView view = composer.compose("R1");

            assertThat(view.announceTime()).isNull();
            assertThat(view.pm10()).isNull();
            assertThat(view.pm25()).isNull();
        }
    }

    private CityRegionCode cityWithSido(Long sidoId) {
        CityRegionCode city = mock(CityRegionCode.class);
        when(city.getSidoRegionCodeId()).thenReturn(sidoId);
        return city;
    }
}