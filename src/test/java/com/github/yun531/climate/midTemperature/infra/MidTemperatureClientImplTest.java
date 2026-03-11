package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.apiKey.ApiKey;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MidTemperatureClientImplTest {

    @Test
    void makeParams_정상() {
        ApiKey apiKey = Mockito.mock(ApiKey.class);
        Mockito.when(apiKey.getApiKey()).thenReturn("abcde");

        MidAnnounceTime announceTime = new MidAnnounceTime(LocalDateTime.of(2026, 3, 9, 12, 0));

        CityRegionCode code = Mockito.mock(CityRegionCode.class);
        Mockito.when(code.getRegionCode()).thenReturn("11B10101");

        MidTemperatureClientImpl midTempClient = new MidTemperatureClientImpl(null, null, apiKey, null);

        // when
        Map<String, String> actual = ReflectionTestUtils.invokeMethod(midTempClient, "makeParams", announceTime, code);

        // then
        assertThat(actual.get("pageNo")).isEqualTo("1");
        assertThat(actual.get("numOfRows")).isEqualTo("1");
        assertThat(actual.get("dataType")).isEqualTo("JSON");
        assertThat(actual.get("regId")).isEqualTo("11B10101");
        assertThat(actual.get("tmFc")).isEqualTo("202603090600");
        assertThat(actual.get("authKey")).isEqualTo("abcde");
    }

    @Test
    void format_정상() {
        // given
        LocalDateTime time = LocalDateTime.of(2026, 3, 9, 12, 0);
        MidTemperatureClientImpl midTempClient = new MidTemperatureClientImpl(null, null, null, null);

        // when
        String actual = ReflectionTestUtils.invokeMethod(midTempClient, "format", time);

        // then
        assertThat(actual).isEqualTo("202603091200");
    }
}