package com.github.yun531.climate.notification.domain.detect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PmAlertThresholds")
class PmAlertThresholdsTest {

    // PM10: moderate=80, bad=150 / PM25: moderate=35, bad=75
    private final PmAlertThresholds thresholds = new PmAlertThresholds(
            new PmAlertThresholds.Thresholds(80, 150),
            new PmAlertThresholds.Thresholds(35, 75));

    @Nested
    @DisplayName("exceedsAlertPm10 -- moderate 초과부터 알림")
    class ExceedsAlertPm10 {

        @ParameterizedTest(name = "값 {0} -> {1}")
        @CsvSource({
                "80, false",
                "81, true",
                "150, true",
                "151, true"
        })
        void exceeds(int value, boolean expected) {
            assertThat(thresholds.exceedsAlertPm10(value)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("exceedsAlertPm25 -- moderate 초과부터 알림")
    class ExceedsAlertPm25 {

        @ParameterizedTest(name = "값 {0} -> {1}")
        @CsvSource({
                "35, false",
                "36, true",
                "75, true",
                "76, true"
        })
        void exceeds(int value, boolean expected) {
            assertThat(thresholds.exceedsAlertPm25(value)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("gradeOfPm10 -- bad 초과면 VERY_BAD")
    class GradeOfPm10 {

        @ParameterizedTest(name = "값 {0} -> {1}")
        @CsvSource({
                "81, BAD",
                "150, BAD",
                "151, VERY_BAD"
        })
        void grades(int value, String expected) {
            assertThat(thresholds.gradeOfPm10(value)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("gradeOfPm25 -- bad 초과면 VERY_BAD")
    class GradeOfPm25 {

        @ParameterizedTest(name = "값 {0} -> {1}")
        @CsvSource({
                "36, BAD",
                "75, BAD",
                "76, VERY_BAD"
        })
        void grades(int value, String expected) {
            assertThat(thresholds.gradeOfPm25(value)).isEqualTo(expected);
        }
    }
}