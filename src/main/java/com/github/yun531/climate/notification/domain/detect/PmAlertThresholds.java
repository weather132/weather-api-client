package com.github.yun531.climate.notification.domain.detect;

/**
 * 미세먼지 알림 판정 임계값.
 */
public record PmAlertThresholds(
        Thresholds pm10,
        Thresholds pm25
) {
    public static final String BAD = "BAD";
    public static final String VERY_BAD = "VERY_BAD";

    public boolean exceedsAlertPm10(int value) {
        return pm10.exceedsAlert(value);
    }

    public boolean exceedsAlertPm25(int value) {
        return pm25.exceedsAlert(value);
    }

    public String gradeOfPm10(int value) {
        return pm10.gradeOf(value);
    }

    public String gradeOfPm25(int value) {
        return pm25.gradeOf(value);
    }

    public record Thresholds(int moderateMax, int badMax) {
        public boolean exceedsAlert(int value) {
            return value > moderateMax;
        }

        public String gradeOf(int value) {
            return value > badMax ? VERY_BAD : BAD;
        }
    }
}