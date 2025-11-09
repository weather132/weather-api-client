package com.github.yun531.climate.weatherApi;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class WeatherApiClientTest {

    @ParameterizedTest
    @CsvSource({
            "1, 23",
            "3, 2",
            "23, 23"
    })
    void nowHourToLatestAnnounceHour(int nowHour, int expected) {
        WeatherApiClient client = new WeatherApiClient(null, null);
        int actual = ReflectionTestUtils.invokeMethod(client, "nowHourToLatestAnnounceHour", nowHour);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 02",
            "15, 15"
    })
    void hourTo2digitHour(int hour, String expected) {
        WeatherApiClient client = new WeatherApiClient(null, null);
        String actual = ReflectionTestUtils.invokeMethod(client, "hourTo2digitHour", hour);
        assertEquals(expected, actual);
    }

}