package com.github.yun531.climate.shortGrid.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.cityRegionCode.domain.Coordinates;
import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShortGridClientImplTest {

    @Test
    void makeParams_정상() {
        // given
        ApiKey apiKey = mock(ApiKey.class);
        when(apiKey.getApiKey()).thenReturn("abcde");

        ShortGridClientImpl shortGridClient = new ShortGridClientImpl(null, null, apiKey, null, null);

        AnnounceTime announceTime = new AnnounceTime(LocalDateTime.of(2026, 3, 12, 12, 0));
        LocalDateTime effectiveTime = LocalDateTime.of(2026, 3, 12, 15, 0);
        String fcstVar = "test";

        // when
        Map<String, String> actual = ReflectionTestUtils.invokeMethod(shortGridClient, "makeParams", announceTime, effectiveTime, fcstVar);

        // then
        assertThat(actual.get("vars")).isEqualTo(fcstVar);
        assertThat(actual.get("authKey")).isEqualTo("abcde");
    }

    @Test
    void format_정상() {
        // given
        ApiKey apiKey = mock(ApiKey.class);
        when(apiKey.getApiKey()).thenReturn("abcde");

        ShortGridClientImpl shortGridClient = new ShortGridClientImpl(null, null, apiKey, null, null);

        LocalDateTime time = LocalDateTime.of(2026, 3, 12, 12, 0);

        // when
        String actual = ReflectionTestUtils.invokeMethod(shortGridClient, "format", time);

        // then
        assertThat(actual).isEqualTo("2026031212");
    }

    @Test
    void getCoords_중복된_좌표_distinct_정상() {
        // given
        Coordinates coords1 = new Coordinates();
        ReflectionTestUtils.setField(coords1, "x", 1);
        ReflectionTestUtils.setField(coords1, "y", 1);

        Coordinates coords2 = new Coordinates();
        ReflectionTestUtils.setField(coords2, "x", 2);
        ReflectionTestUtils.setField(coords2, "y", 2);

        Coordinates coords3 = new Coordinates();
        ReflectionTestUtils.setField(coords3, "x", 2);
        ReflectionTestUtils.setField(coords3, "y", 2);

        CityRegionCode city1 = mock(CityRegionCode.class);
        when(city1.getCoordinates()).thenReturn(coords1);

        CityRegionCode city2 = mock(CityRegionCode.class);
        when(city2.getCoordinates()).thenReturn(coords2);

        CityRegionCode city3 = mock(CityRegionCode.class);
        when(city3.getCoordinates()).thenReturn(coords3);

        List<CityRegionCode> cityRegions = List.of(city1, city2, city3);

        CityRegionCodeRepository cityRegionRepo = mock(CityRegionCodeRepository.class);
        when(cityRegionRepo.findAll()).thenReturn(cityRegions);

        ShortGridClientImpl client = new ShortGridClientImpl(null, null, null, null, cityRegionRepo);

        // when
        List<Coordinates> actual = ReflectionTestUtils.invokeMethod(client, "getCoords");

        // then
        assertThat(actual).hasSize(2);
    }
}