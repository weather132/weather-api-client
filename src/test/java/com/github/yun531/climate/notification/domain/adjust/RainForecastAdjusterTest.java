package com.github.yun531.climate.notification.domain.adjust;

import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload.DailyRainFlags;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload.RainInterval;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RainForecastAdjusterTest {

    private final RainForecastAdjuster adjuster = new RainForecastAdjuster(2, 24, 1);

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 1, 22, 5, 0);


    @Nested
    @DisplayName("null 가드")
    class NullGuards {

        @Test
        @DisplayName("null event -> null 반환")
        void nullEvent() {
            assertThat(adjuster.adjust(null, ANNOUNCE_TIME, ANNOUNCE_TIME)).isNull();
        }

        @Test
        @DisplayName("null announceTime -> 원본 반환")
        void nullAnnounceTime() {
            AlertEvent event = makeAlertEvent(List.of());

            assertThat(adjuster.adjust(event, null, ANNOUNCE_TIME)).isEqualTo(event);
        }

        @Test
        @DisplayName("null now -> 원본 반환")
        void nullNow() {
            AlertEvent event = makeAlertEvent(List.of(
                    new RainInterval(ANNOUNCE_TIME.plusHours(2), ANNOUNCE_TIME.plusHours(5))));

            assertThat(adjuster.adjust(event, ANNOUNCE_TIME, null)).isEqualTo(event);
        }
    }


    @Nested
    @DisplayName("윈도우 클리핑")
    class WindowClipping {

        @Test
        @DisplayName("시프트 없음 -> 윈도우 밖 구간만 제거")
        void noShift_clippingOnly() {
            RainInterval inside  = new RainInterval(ANNOUNCE_TIME.plusHours(2), ANNOUNCE_TIME.plusHours(5));
            RainInterval outside = new RainInterval(ANNOUNCE_TIME.plusHours(25), ANNOUNCE_TIME.plusHours(26));

            AlertEvent result = adjuster.adjust(
                    makeAlertEvent(List.of(inside, outside)), ANNOUNCE_TIME, ANNOUNCE_TIME);

            RainForecastPayload payload = extractPayload(result);
            assertThat(payload.hourlyParts()).hasSize(1);
            assertThat(payload.hourlyParts().get(0).start()).isEqualTo(ANNOUNCE_TIME.plusHours(2));
            assertThat(payload.hourlyParts().get(0).end()).isEqualTo(ANNOUNCE_TIME.plusHours(5));
        }

        @Test
        @DisplayName("1시간 시프트 -> 윈도우 밖 구간 제거, 안쪽 구간 보존")
        void oneHourShift_removesOutside_preservesInside() {
            LocalDateTime now = ANNOUNCE_TIME.plusHours(1);
            RainInterval outside = new RainInterval(ANNOUNCE_TIME, ANNOUNCE_TIME.plusHours(1));
            RainInterval inside  = new RainInterval(ANNOUNCE_TIME.plusHours(4), ANNOUNCE_TIME.plusHours(6));

            AlertEvent result = adjuster.adjust(
                    makeAlertEvent(List.of(outside, inside)), ANNOUNCE_TIME, now);

            RainForecastPayload payload = extractPayload(result);
            assertThat(payload.hourlyParts()).hasSize(1);
            assertThat(payload.hourlyParts().get(0).start()).isEqualTo(ANNOUNCE_TIME.plusHours(4));
            assertThat(payload.hourlyParts().get(0).end()).isEqualTo(ANNOUNCE_TIME.plusHours(6));
        }

        @Test
        @DisplayName("윈도우 경계에 걸치는 구간 -> 클램핑")
        void overlapping_clamped() {
            LocalDateTime now = ANNOUNCE_TIME.plusHours(1);
            RainInterval overlapping = new RainInterval(ANNOUNCE_TIME.plusHours(1), ANNOUNCE_TIME.plusHours(4));

            AlertEvent result = adjuster.adjust(
                    makeAlertEvent(List.of(overlapping)), ANNOUNCE_TIME, now);

            RainForecastPayload payload = extractPayload(result);
            assertThat(payload.hourlyParts()).hasSize(1);
            assertThat(payload.hourlyParts().get(0).start()).isEqualTo(ANNOUNCE_TIME.plusHours(2));
            assertThat(payload.hourlyParts().get(0).end()).isEqualTo(ANNOUNCE_TIME.plusHours(4));
        }
    }


    @Nested
    @DisplayName("maxShift 클램프")
    class MaxShift {

        @Test
        @DisplayName("실제 shift > maxShift -> maxShift로 클램프")
        void clamped() {
            LocalDateTime now = ANNOUNCE_TIME.plusHours(5);
            RainInterval interval = new RainInterval(ANNOUNCE_TIME.plusHours(8), ANNOUNCE_TIME.plusHours(10));

            AlertEvent result = adjuster.adjust(makeAlertEvent(List.of(interval)), ANNOUNCE_TIME, now);

            assertThat(result.occurredAt()).isEqualTo(ANNOUNCE_TIME.plusHours(2));
        }
    }


    @Nested
    @DisplayName("dayShift")
    class DayShift {

        @Test
        @DisplayName("날짜 경계 넘으면 dayParts 앞쪽 드롭")
        void dropsFrontDays() {
            LocalDateTime announceTime = LocalDateTime.of(2026, 1, 22, 23, 0);
            LocalDateTime now = announceTime.plusHours(2);

            List<DailyRainFlags> days = List.of(
                    new DailyRainFlags(true, false),
                    new DailyRainFlags(false, true),
                    new DailyRainFlags(true, true),
                    new DailyRainFlags(false, false),
                    new DailyRainFlags(false, false),
                    new DailyRainFlags(false, false),
                    new DailyRainFlags(false, false));

            AlertEvent result = adjuster.adjust(
                    makeAlertEvent(List.of(), days), announceTime, now);

            RainForecastPayload payload = extractPayload(result);
            assertThat(payload.dayParts().get(0).rainAm()).isFalse();
            assertThat(payload.dayParts().get(0).rainPm()).isTrue();
        }
    }

    // ==================== helper ====================

    private static final List<DailyRainFlags> EMPTY_DAYS = List.of(
            new DailyRainFlags(false, false), new DailyRainFlags(false, false),
            new DailyRainFlags(false, false), new DailyRainFlags(false, false),
            new DailyRainFlags(false, false), new DailyRainFlags(false, false),
            new DailyRainFlags(false, false));

    private RainForecastPayload extractPayload(AlertEvent event) {
        assertThat(event).isNotNull();
        return (RainForecastPayload) event.payload();
    }

    private AlertEvent makeAlertEvent(List<RainInterval> hourly) {
        return makeAlertEvent(hourly, EMPTY_DAYS);
    }

    private AlertEvent makeAlertEvent(List<RainInterval> hourly, List<DailyRainFlags> days) {
        RainForecastPayload payload = new RainForecastPayload(hourly, days);
        return new AlertEvent(AlertTypeEnum.RAIN_FORECAST, "R1", ANNOUNCE_TIME, payload);
    }
}