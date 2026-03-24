package com.github.yun531.climate.notification.domain.detect;

import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload;
import com.github.yun531.climate.notification.domain.readmodel.PopView;
import com.github.yun531.climate.notification.domain.readmodel.PopView.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
class RainForecastDetectorTest {

    private static final int THRESHOLD  = 60;
    private static final int MAX_POINTS = 26;
    private final RainForecastDetector detector = new RainForecastDetector(THRESHOLD, MAX_POINTS);

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 1, 22, 5, 0);
    private static final LocalDateTime NOW           = LocalDateTime.of(2026, 1, 22, 5, 15);


    @Nested
    @DisplayName("null/blank 가드")
    class NullBlankGuards {

        @Test
        @DisplayName("null 입력 -> null 반환")
        void nullInputs() {
            assertThat(detector.detect(null, buildPopView(ANNOUNCE_TIME, new Integer[26]), NOW)).isNull();
            assertThat(detector.detect("R1", null, NOW)).isNull();
            assertThat(detector.detect("R1", buildPopView(ANNOUNCE_TIME, new Integer[26]), null)).isNull();
        }

        @Test
        @DisplayName("blank regionId -> null 반환")
        void blankRegionId() {
            PopView view = buildPopView(ANNOUNCE_TIME, new Integer[26]);

            assertThat(detector.detect("", view, NOW)).isNull();
            assertThat(detector.detect("   ", view, NOW)).isNull();
        }
    }


    @Nested
    @DisplayName("hourly 비 구간 탐지")
    class HourlyRain {

        @Test
        @DisplayName("비 없음 -> hourlyParts 비어 있음")
        void noRain() {
            PopView view = buildPopView(ANNOUNCE_TIME, new Integer[26]);

            AlertEvent event = detector.detect("R1", view, NOW);

            assertThat(event).isNotNull();
            assertThat(extractPayload(event).hourlyParts()).isEmpty();
        }

        @Test
        @DisplayName("연속 비 구간 -> RainInterval 1개")
        void consecutiveRain() {
            Integer[] pops = new Integer[26];
            pops[3] = THRESHOLD; pops[4] = THRESHOLD + 20; pops[5] = THRESHOLD + 30;
            PopView view = buildPopView(ANNOUNCE_TIME, pops);

            AlertEvent event = detector.detect("R1", view, NOW);

            assertThat(event).isNotNull();
            assertThat(event.type()).isEqualTo(AlertTypeEnum.RAIN_FORECAST);
            RainForecastPayload payload = extractPayload(event);
            assertThat(payload.hourlyParts()).hasSize(1);
            assertThat(payload.hourlyParts().get(0).start()).isEqualTo(ANNOUNCE_TIME.plusHours(4));
            assertThat(payload.hourlyParts().get(0).end()).isEqualTo(ANNOUNCE_TIME.plusHours(6));
        }

        @Test
        @DisplayName("끊어진 비 구간 -> RainInterval 2개")
        void multipleSegments() {
            Integer[] pops = new Integer[26];
            pops[0] = THRESHOLD; pops[1] = THRESHOLD + 10;
            pops[4] = THRESHOLD + 30; pops[5] = THRESHOLD + 10;
            PopView view = buildPopView(ANNOUNCE_TIME, pops);

            AlertEvent event = detector.detect("R1", view, NOW);

            assertThat(event).isNotNull();
            assertThat(extractPayload(event).hourlyParts()).hasSize(2);
        }

        @Test
        @DisplayName("effectiveTime null -> 스킵 후 루프 지속")
        void nullEffectiveTime_skipped() {
            List<Hourly.Pop> hourlyPops = new ArrayList<>();
            hourlyPops.add(new Hourly.Pop(ANNOUNCE_TIME.plusHours(1), THRESHOLD + 10));
            hourlyPops.add(new Hourly.Pop(null, THRESHOLD + 20));
            hourlyPops.add(new Hourly.Pop(ANNOUNCE_TIME.plusHours(3), THRESHOLD + 10));
            for (int i = 3; i < 26; i++) {
                hourlyPops.add(new Hourly.Pop(ANNOUNCE_TIME.plusHours(i + 1), null));
            }

            PopView view = new PopView(new Hourly(hourlyPops), buildEmptyDaily(), ANNOUNCE_TIME);

            AlertEvent event = detector.detect("R1", view, NOW);

            assertThat(event).isNotNull();
            assertThat(extractPayload(event).hourlyParts()).hasSize(2);
        }
    }


    @Nested
    @DisplayName("구간 닫기 / 절단")
    class SegmentClosing {

        @Test
        @DisplayName("마지막 포인트까지 비 -> 열린 구간 자동 닫기")
        void lastPointRainy_closes() {
            Integer[] pops = new Integer[26];
            pops[24] = THRESHOLD + 20; pops[25] = THRESHOLD + 10;
            PopView view = buildPopView(ANNOUNCE_TIME, pops);

            AlertEvent event = detector.detect("R1", view, NOW);

            RainForecastPayload payload = extractPayload(event);
            assertThat(payload.hourlyParts()).hasSize(1);
            assertThat(payload.hourlyParts().get(0).start()).isEqualTo(ANNOUNCE_TIME.plusHours(25));
            assertThat(payload.hourlyParts().get(0).end()).isEqualTo(ANNOUNCE_TIME.plusHours(26));
        }

        @Test
        @DisplayName("maxHourlyPoints 초과 시 절단")
        void maxPoints_truncated() {
            RainForecastDetector smallDetector = new RainForecastDetector(THRESHOLD, 5);

            Integer[] pops = new Integer[26];
            pops[0] = THRESHOLD + 10; pops[1] = THRESHOLD + 20; pops[2] = THRESHOLD + 30;
            pops[4] = THRESHOLD + 10; pops[5] = THRESHOLD + 20; pops[6] = THRESHOLD + 30;
            PopView view = buildPopView(ANNOUNCE_TIME, pops);

            AlertEvent event = smallDetector.detect("R1", view, NOW);

            RainForecastPayload payload = extractPayload(event);
            assertThat(payload.hourlyParts()).hasSize(2);
            assertThat(payload.hourlyParts().get(1).start())
                    .isEqualTo(payload.hourlyParts().get(1).end());
        }
    }


    @Nested
    @DisplayName("daily 비 판정")
    class DailyRain {

        @Test
        @DisplayName("AM >= threshold, PM < threshold -> rainAm=true, rainPm=false")
        void amPmMapping() {
            PopView view = buildPopViewWithDaily(ANNOUNCE_TIME, new Integer[26],
                    THRESHOLD + 10, THRESHOLD - 30);

            RainForecastPayload payload = extractPayload(detector.detect("R1", view, NOW));

            assertThat(payload.dayParts()).isNotEmpty();
            assertThat(payload.dayParts().get(0).rainAm()).isTrue();
            assertThat(payload.dayParts().get(0).rainPm()).isFalse();
        }

        @Test
        @DisplayName("AM/PM 모두 null -> 모두 false")
        void nullPop_allFalse() {
            PopView view = buildPopViewWithDaily(ANNOUNCE_TIME, new Integer[26], null, null);

            RainForecastPayload payload = extractPayload(detector.detect("R1", view, NOW));

            assertThat(payload.dayParts().get(0).rainAm()).isFalse();
            assertThat(payload.dayParts().get(0).rainPm()).isFalse();
        }
    }


    @Nested
    @DisplayName("occurredAt 계산")
    class OccurredAt {

        @Test
        @DisplayName("announceTime 존재 -> occurredAt 으로 사용")
        void announceTimePresent() {
            LocalDateTime reportTime = LocalDateTime.of(2026, 1, 22, 5, 30);
            Integer[] pops = new Integer[26];
            pops[0] = THRESHOLD + 10;
            PopView view = buildPopViewWithDaily(reportTime, pops, null, null);

            assertThat(detector.detect("R1", view, NOW).occurredAt()).isEqualTo(reportTime);
        }

        @Test
        @DisplayName("announceTime null -> now 사용")
        void announceTimeNull() {
            Integer[] pops = new Integer[26];
            pops[0] = THRESHOLD;
            PopView view = buildPopViewWithDaily(null, pops, null, null);

            assertThat(detector.detect("R1", view, NOW).occurredAt()).isEqualTo(NOW);
        }
    }

    // ==================== helper ====================

    private RainForecastPayload extractPayload(AlertEvent event) {
        assertThat(event).isNotNull();
        return (RainForecastPayload) event.payload();
    }

    private PopView buildPopView(LocalDateTime reportTime, Integer[] pops) {
        return buildPopViewWithDaily(reportTime, pops, null, null);
    }

    private PopView buildPopViewWithDaily(LocalDateTime reportTime, Integer[] pops,
                                          Integer day0AmPop, Integer day0PmPop) {
        LocalDateTime baseTime = reportTime != null ? reportTime : ANNOUNCE_TIME;
        List<Hourly.Pop> hourlyPops = new ArrayList<>(26);
        for (int i = 0; i < 26; i++) {
            Integer pop = (i < pops.length) ? pops[i] : null;
            hourlyPops.add(new Hourly.Pop(baseTime.plusHours(i + 1), pop));
        }

        List<Daily.Pop> dailyPops = new ArrayList<>(7);
        dailyPops.add(new Daily.Pop(day0AmPop, day0PmPop));
        for (int i = 1; i < 7; i++) dailyPops.add(new Daily.Pop(null, null));

        return new PopView(new Hourly(hourlyPops), new Daily(dailyPops), reportTime);
    }

    private Daily buildEmptyDaily() {
        List<Daily.Pop> days = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) days.add(new Daily.Pop(null, null));
        return new Daily(days);
    }
}