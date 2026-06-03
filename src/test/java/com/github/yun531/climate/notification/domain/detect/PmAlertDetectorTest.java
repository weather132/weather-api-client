package com.github.yun531.climate.notification.domain.detect;

import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.AirPollutionPayload;
import com.github.yun531.climate.notification.domain.readmodel.AirQualityView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PmAlertDetector")
class PmAlertDetectorTest {

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 5, 21, 11, 0);

    // PM10: moderate=80, bad=150 / PM25: moderate=35, bad=75
    private final PmAlertThresholds thresholds = new PmAlertThresholds(
            new PmAlertThresholds.Thresholds(80, 150),
            new PmAlertThresholds.Thresholds(35, 75));

    private final PmAlertDetector detector = new PmAlertDetector(thresholds);

    @Nested
    @DisplayName("임계 이하 -- 알림 없음")
    class BelowThreshold {

        @ParameterizedTest(name = "pm10={0}, pm25={1} -> 알림 없음")
        @CsvSource({
                "50, 20",   // 둘 다 임계 이하
                "80, 35"    // moderate 경계값 (초과 아님)
        })
        void noAlert(int pm10, int pm25) {
            AirQualityView view = new AirQualityView(ANNOUNCE_TIME, pm10, pm25);

            assertThat(detector.detect("R1", view)).isEmpty();
        }
    }

    @Nested
    @DisplayName("임계 초과")
    class AboveThreshold {

        @Test
        @DisplayName("PM10만 초과 -> PM10 알림 1건, payload 검증")
        void pm10Only() {
            AirQualityView view = new AirQualityView(ANNOUNCE_TIME, 95, 20);

            List<AlertEvent> alerts = detector.detect("R1", view);

            assertThat(alerts).hasSize(1);
            AlertEvent event = alerts.get(0);
            assertThat(event.type()).isEqualTo(AlertTypeEnum.AIR_POLLUTION);
            assertThat(event.regionId()).isEqualTo("R1");

            AirPollutionPayload payload = (AirPollutionPayload) event.payload();
            assertThat(payload.pollutionType()).isEqualTo("PM10");
            assertThat(payload.value()).isEqualTo(95);
            assertThat(payload.grade()).isEqualTo("BAD");
            assertThat(payload.announceTime()).isEqualTo(ANNOUNCE_TIME);
        }

        @Test
        @DisplayName("PM10 VERY_BAD + PM25 BAD -> 알림 2건")
        void bothExceed() {
            AirQualityView view = new AirQualityView(ANNOUNCE_TIME, 200, 50);

            List<AlertEvent> alerts = detector.detect("R1", view);

            assertThat(alerts).hasSize(2);
            assertThat(alerts).extracting(e -> ((AirPollutionPayload) e.payload()).pollutionType())
                    .containsExactly("PM10", "PM25");
            assertThat(alerts).extracting(e -> ((AirPollutionPayload) e.payload()).grade())
                    .containsExactly("VERY_BAD", "BAD");
        }

        @Test
        @DisplayName("PM25만 VERY_BAD -> PM25 알림 1건")
        void pm25Only() {
            AirQualityView view = new AirQualityView(ANNOUNCE_TIME, 30, 100);

            List<AlertEvent> alerts = detector.detect("R1", view);

            assertThat(alerts).hasSize(1);
            AirPollutionPayload payload = (AirPollutionPayload) alerts.get(0).payload();
            assertThat(payload.pollutionType()).isEqualTo("PM25");
            assertThat(payload.grade()).isEqualTo("VERY_BAD");
        }
    }
}