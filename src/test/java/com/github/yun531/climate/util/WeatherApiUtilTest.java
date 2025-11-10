package com.github.yun531.climate.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class WeatherApiUtilTest {

    @ParameterizedTest
    @CsvSource({
            "1, 23",
            "3, 2",
            "23, 23"
    })
    void nowHourToShortTermLatestAnnounceHour(int nowHour, int expected) {
        WeatherApiUtil util = new WeatherApiUtil();
        int actual = ReflectionTestUtils.invokeMethod(util, "nowHourToShortTermLatestAnnounceHour", nowHour);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 02",
            "15, 15"
    })
    void hourTo2digitHour(int hour, String expected) {
        WeatherApiUtil util = new WeatherApiUtil();
        String actual = ReflectionTestUtils.invokeMethod(util, "hourTo2digitHour", hour);
        assertEquals(expected, actual);
    }

}