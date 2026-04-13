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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        stubShortLandBatch(70, 22);

        List<DailyForecastItem> result = composer.compose(regionCode);

        assertThat(result).hasSize(14)
                .allMatch(item -> item.getPop() == 70 && item.getTemp() == 22);
        assertThat(result.stream().filter(i -> i.getEffectiveTime().getHour() == 9).count())
                .isEqualTo(7);
        assertThat(result.stream().filter(i -> i.getEffectiveTime().getHour() == 21).count())
                .isEqualTo(7);
        verify(midLandRepository, never()).findRecentAll(any(), any());
        verify(midTemperatureRepository, never()).findRecentAll(any(), any());
    }

    @Test
    @DisplayName("ShortLand 빈 결과 → Mid fallback, 강수확률은 MidLand 값 사용")
    void mid_fallback_pop() {
        stubMidFallbackBatch(80, 25, 10);

        List<DailyForecastItem> result = composer.compose(regionCode);

        assertThat(result).hasSize(14)
                .allMatch(item -> item.getPop() == 80);
    }

    @Test
    @DisplayName("Mid fallback 시 오전은 minTemp, 오후는 maxTemp 매핑")
    void mid_fallback_temp_by_time_of_day() {
        stubMidFallbackBatch(80, 25, 10);

        List<DailyForecastItem> result = composer.compose(regionCode);

        assertThat(result).hasSize(14)
                .allMatch(item -> item.getPop() == 80);

        assertThat(result.stream().filter(i -> i.getEffectiveTime().getHour() == 9))
                .allMatch(i -> i.getTemp() == 10);
        assertThat(result.stream().filter(i -> i.getEffectiveTime().getHour() == 21))
                .allMatch(i -> i.getTemp() == 25);
    }

    // ==================== helper ====================

    private void stubShortLandBatch(int pop, int temp) {
        when(shortLandRepository.findRecentAll(eq(regionCode), any()))
                .thenAnswer(invocation -> {
                    List<LocalDateTime> times = invocation.getArgument(1);
                    return times.stream().collect(Collectors.toMap(
                            et -> et,
                            et -> new ShortLand(
                                    LocalDateTime.of(2026, 3, 28, 17, 0),
                                    et, CITY_ID, pop, temp, 0)
                    ));
                });
    }

    private void stubMidFallbackBatch(int pop, int maxTemp, int minTemp) {
        // ShortLand 배치 → 빈 Map
        when(shortLandRepository.findRecentAll(eq(regionCode), any()))
                .thenReturn(Map.of());

        // ProvinceRegionCode 1회
        when(provinceRegionCodeRepository.findById(PROVINCE_ID))
                .thenReturn(Optional.of(provinceRegionCode));

        MidAnnounceTime midAnnounceTime = new MidAnnounceTime(
                LocalDateTime.of(2026, 3, 28, 12, 0));

        // MidTemperature 배치 → morning times에 대해 Map 반환
        when(midTemperatureRepository.findRecentAll(eq(regionCode), any()))
                .thenAnswer(invocation -> {
                    List<LocalDateTime> times = invocation.getArgument(1);
                    return times.stream().collect(Collectors.toMap(
                            et -> et,
                            et -> new MidTemperature(midAnnounceTime, et, CITY_ID, maxTemp, minTemp)
                    ));
                });

        // MidLand 배치 → 전체 missing times에 대해 Map 반환
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