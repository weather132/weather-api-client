package com.github.yun531.climate.forecast.domain.readmodel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AirQualityGradeThresholds")
class AirQualityGradeThresholdsTest {

    // PM10: good=30, moderate=80, bad=150 / PM25: good=15, moderate=35, bad=75
    private final AirQualityGradeThresholds thresholds = new AirQualityGradeThresholds(
            new AirQualityGradeThresholds.Thresholds(30, 80, 150),
            new AirQualityGradeThresholds.Thresholds(15, 35, 75));

    @Nested
    @DisplayName("classifyPm10 경계값")
    class ClassifyPm10 {

        @ParameterizedTest(name = "값 {0} -> {1}")
        @CsvSource({
                "0, GOOD",
                "30, GOOD",
                "31, MODERATE",
                "80, MODERATE",
                "81, BAD",
                "150, BAD",
                "151, VERY_BAD"
        })
        void classifies(int value, String expected) {
            assertThat(thresholds.classifyPm10(value)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("classifyPm25 경계값")
    class ClassifyPm25 {

        @ParameterizedTest(name = "값 {0} -> {1}")
        @CsvSource({
                "0, GOOD",
                "15, GOOD",
                "16, MODERATE",
                "35, MODERATE",
                "36, BAD",
                "75, BAD",
                "76, VERY_BAD"
        })
        void classifies(int value, String expected) {
            assertThat(thresholds.classifyPm25(value)).isEqualTo(expected);
        }
    }
}