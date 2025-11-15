package com.github.yun531.climate.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class WeatherApiUtilTest {

    @ParameterizedTest
    @CsvSource({
            "2, 0",
            "1, 2",
            "0, 1"
    })
    void pastHoursSinceLatestShortTermAnnouncement(int nowHour, int expected) {
        WeatherApiUtil util = new WeatherApiUtil();
        int actual = ReflectionTestUtils.invokeMethod(util, "pastHoursSinceLatestShortTermAnnouncement", nowHour);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "6, 0",
            "18, 0",
            "0, 6",
            "5, 11",
            "23, 5"
    })
    void pastHoursSinceLatestMidTermAnnouncement(int nowHour, int expected) {
        WeatherApiUtil util = new WeatherApiUtil();
        int actual = ReflectionTestUtils.invokeMethod(util, "pastHoursSinceLatestMidTermAnnouncement", nowHour);
        assertEquals(expected, actual);
    }

}