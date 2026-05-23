package com.github.yun531.climate.forecast.domain.readmodel;

/**
 * 미세먼지 등급 임계값. PM10/PM25 각각의 good/moderate/bad 경계로 등급을 분류.
 */
public record AirQualityGradeThresholds(
        Thresholds pm10,
        Thresholds pm25
) {
    public static final String GOOD = "GOOD";
    public static final String MODERATE = "MODERATE";
    public static final String BAD = "BAD";
    public static final String VERY_BAD = "VERY_BAD";

    public String classifyPm10(int value) {
        return pm10.classify(value);
    }

    public String classifyPm25(int value) {
        return pm25.classify(value);
    }

    public record Thresholds(int goodMax, int moderateMax, int badMax) {
        public String classify(int value) {
            if (value <= goodMax) return GOOD;
            if (value <= moderateMax) return MODERATE;
            if (value <= badMax) return BAD;
            return VERY_BAD;
        }
    }
}