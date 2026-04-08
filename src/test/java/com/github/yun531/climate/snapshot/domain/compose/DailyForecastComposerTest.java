package com.github.yun531.climate.snapshot.domain.compose;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandRepository;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureRepository;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCodeRepository;
import com.github.yun531.climate.shortLand.domain.ShortLand;
import com.github.yun531.climate.shortLand.domain.ShortLandRepository;
import com.github.yun531.climate.snapshot.domain.model.DailyForecastItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @BeforeEach
    void setUp() {
        composer = new DailyForecastComposer(
                shortLandRepository, midLandRepository,
                midTemperatureRepository, provinceRegionCodeRepository);

        lenient().when(regionCode.getId()).thenReturn(CITY_ID);
        lenient().when(regionCode.getProvinceRegionCodeId()).thenReturn(PROVINCE_ID);
    }

    @Test
    @DisplayName("ShortLand 데이터 존재 시 ShortLand 값으로 14개 생성, Mid 미호출")
    void shortLand_path() {
        stubShortLand(70, 22);

        List<DailyForecastItem> result = composer.compose(regionCode);

        assertThat(result).hasSize(14)
                .allMatch(item -> item.getPop() == 70 && item.getTemp() == 22);
        assertThat(result.stream().filter(i -> i.getEffectiveTime().getHour() == 9).count())
                .isEqualTo(7);
        assertThat(result.stream().filter(i -> i.getEffectiveTime().getHour() == 21).count())
                .isEqualTo(7);
        verify(midLandRepository, never()).findRecent(any(), any());
        verify(midTemperatureRepository, never()).findRecent(any(), any());
    }

    @Test
    @DisplayName("ShortLand null → Mid fallback, 강수확률은 MidLand 값 사용")
    void mid_fallback_pop() {
        stubMidFallback(80, 25, 10);

        List<DailyForecastItem> result = composer.compose(regionCode);

        assertThat(result).hasSize(14)
                .allMatch(item -> item.getPop() == 80);
    }

    @Test
    @DisplayName("Mid fallback 시 오전은 minTemp, 오후는 maxTemp 매핑")
    void mid_fallback_temp_by_time_of_day() {
        stubMidFallback(80, 25, 10);

        List<DailyForecastItem> result = composer.compose(regionCode);

        assertThat(result).hasSize(14)
                .allMatch(item -> item.getPop() == 80);

        assertThat(result.stream().filter(i -> i.getEffectiveTime().getHour() == 9))
                .allMatch(i -> i.getTemp() == 10);
        assertThat(result.stream().filter(i -> i.getEffectiveTime().getHour() == 21))
                .allMatch(i -> i.getTemp() == 25);
    }

    @Test
    void composeDailyForecastItemFromMid_정상() {
        // given
        MidAnnounceTime announceTime = new MidAnnounceTime(LocalDateTime.of(2026, 4, 8, 6, 0));
        LocalDateTime morningEf = LocalDateTime.of(2026, 4, 8, 9, 0);
        LocalDateTime afternoonEf = LocalDateTime.of(2026, 4, 8, 21, 0);

        MidTemperature midTemp = new MidTemperature(announceTime, morningEf, CITY_ID, 10, 0);

        when(midTemperatureRepository.findRecent(regionCode, morningEf)).thenReturn(midTemp);

        when(provinceRegionCodeRepository.findById(any())).thenReturn(Optional.ofNullable(provinceRegionCode));

        MidLand morningLand = new MidLand(announceTime, morningEf, PROVINCE_ID, 1);
        MidLand afternoonLand = new MidLand(announceTime, afternoonEf, PROVINCE_ID, 2);

        when(midLandRepository.findRecent(provinceRegionCode, morningEf)).thenReturn(morningLand);
        when(midLandRepository.findRecent(provinceRegionCode, afternoonEf)).thenReturn(afternoonLand);

        DailyForecastComposer composer1 = new DailyForecastComposer(shortLandRepository, midLandRepository, midTemperatureRepository, provinceRegionCodeRepository);

        // when
        DailyForecastItem morningActual = ReflectionTestUtils.invokeMethod(composer1, "composeDailyForecastItemFromMid", regionCode, morningEf);
        DailyForecastItem afternoonActual = ReflectionTestUtils.invokeMethod(composer1, "composeDailyForecastItemFromMid", regionCode, afternoonEf);

        // then
        assertThat(morningActual.getTemp()).isEqualTo(0);
        assertThat(morningActual.getPop()).isEqualTo(1);

        assertThat(afternoonActual.getTemp()).isEqualTo(10);
        assertThat(afternoonActual.getPop()).isEqualTo(2);
    }

    // ==================== helper ====================

    private void stubShortLand(int pop, int temp) {
        when(shortLandRepository.findRecent(eq(regionCode), any()))
                .thenAnswer(invocation -> {
                    LocalDateTime efTime = invocation.getArgument(1);
                    return new ShortLand(
                            LocalDateTime.of(2026, 3, 28, 17, 0),
                            efTime, CITY_ID, pop, temp, 0);
                });
    }

    private void stubMidFallback(int pop, int maxTemp, int minTemp) {
        when(shortLandRepository.findRecent(eq(regionCode), any()))
                .thenReturn(null);

        MidAnnounceTime midAnnounceTime = new MidAnnounceTime(
                LocalDateTime.of(2026, 3, 28, 12, 0));

        when(provinceRegionCodeRepository.findById(PROVINCE_ID))
                .thenReturn(Optional.of(provinceRegionCode));
        when(midLandRepository.findRecent(eq(provinceRegionCode), any()))
                .thenReturn(new MidLand(midAnnounceTime,
                        LocalDateTime.of(2026, 3, 29, 9, 0), PROVINCE_ID, pop));
        when(midTemperatureRepository.findRecent(eq(regionCode), any()))
                .thenReturn(new MidTemperature(midAnnounceTime,
                        LocalDateTime.of(2026, 3, 29, 9, 0), CITY_ID, maxTemp, minTemp));
    }
}