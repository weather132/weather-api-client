package com.github.yun531.climate.shortGrid.infra;

import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
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
}