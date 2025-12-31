package com.github.yun531.climate.weatherApi;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WeatherApiClientTest {

    @Test
    void getShortGridParameters() {
        // given
        WeatherApiClient client = new WeatherApiClient(null, "APIKEY");
        String fcstVar = "VAR";
        LocalDateTime announceTime = LocalDateTime.of(1972, 11, 21, 10, 0);
        LocalDateTime targetTime = LocalDateTime.of(1972, 11, 21, 11, 0);

        // when
        Map<String, String> params = ReflectionTestUtils.invokeMethod(client, "getShortTermParameters", fcstVar, announceTime, targetTime);

        // then
        assertTrue(params.containsValue(fcstVar));
        assertTrue(params.containsValue("APIKEY"));
        assertTrue(params.containsValue("1972112110"));
        assertTrue(params.containsValue("1972112111"));


    }

}