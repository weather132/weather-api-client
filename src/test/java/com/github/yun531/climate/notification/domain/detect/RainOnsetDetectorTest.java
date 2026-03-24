package com.github.yun531.climate.notification.domain.detect;

import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.RainOnsetPayload;
import com.github.yun531.climate.notification.domain.readmodel.PopView;
import com.github.yun531.climate.notification.domain.readmodel.PopView.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RainOnsetDetectorTest {

    private static final int THRESHOLD  = 60;
    private static final int MAX_POINTS = 26;

    private final RainOnsetDetector detector = new RainOnsetDetector(THRESHOLD, MAX_POINTS);

    private static final LocalDateTime ANNOUNCE_TIME     = LocalDateTime.of(2026, 1, 22, 5, 0);
    private static final LocalDateTime NOW               = LocalDateTime.of(2026, 1, 22, 5, 15);
    private static final LocalDateTime SERIES_START_TIME = ANNOUNCE_TIME.plusHours(1);

    @Nested
    @DisplayName("null/blank 가드")
    class NullGuards {

        @Test
        @DisplayName("pair null -> 빈 리스트")
        void nullPair() {
            assertThat(detector.detect("R1", null, NOW)).isEmpty();
        }

        @Test
        @DisplayName("regionId null/blank -> 빈 리스트")
        void nullOrBlankRegionId() {
            PopView view = buildPopView(ANNOUNCE_TIME, SERIES_START_TIME, toIntegers(THRESHOLD));
            PopView.Pair pair = new PopView.Pair(view, view);

            assertThat(detector.detect(null, pair, NOW)).isEmpty();
            assertThat(detector.detect("", pair, NOW)).isEmpty();
            assertThat(detector.detect("   ", pair, NOW)).isEmpty();
        }

        @Test
        @DisplayName("pair 내부 current/previous null -> 빈 리스트")
        void nullCurrentOrPrevious() {
            PopView view = buildPopView(ANNOUNCE_TIME, SERIES_START_TIME, toIntegers(THRESHOLD));

            assertThat(detector.detect("R1", new PopView.Pair(null, view), NOW)).isEmpty();
            assertThat(detector.detect("R1", new PopView.Pair(view, null), NOW)).isEmpty();
        }
    }

    @Nested
    @DisplayName("onset 감지")
    class OnsetDetection {

        @Test
        @DisplayName("이전 < threshold, 현재 >= threshold -> onset")
        void prevBelow_curAbove() {
            PopView prev = buildPopView(ANNOUNCE_TIME.minusHours(3), SERIES_START_TIME,
                    toIntegers(THRESHOLD - 10, THRESHOLD - 10, THRESHOLD - 10));
            PopView cur = buildPopView(ANNOUNCE_TIME, SERIES_START_TIME,
                    toIntegers(THRESHOLD - 10, THRESHOLD, THRESHOLD));

            List<AlertEvent> events = detector.detect("R1", new PopView.Pair(cur, prev), NOW);

            assertThat(events).hasSize(2);
            assertThat(events).allMatch(e -> e.type() == AlertTypeEnum.RAIN_ONSET);
            assertThat(events.get(0).payload()).isInstanceOf(RainOnsetPayload.class);
        }

        @Test
        @DisplayName("이전 예보에 해당 시각 없으면 현재 POP 만으로 판정")
        void noPrevData_curAbove() {
            PopView prev = buildPopView(ANNOUNCE_TIME.minusHours(3), SERIES_START_TIME,
                    toIntegers(THRESHOLD - 10, THRESHOLD - 10));
            PopView cur = buildPopView(ANNOUNCE_TIME, SERIES_START_TIME,
                    toIntegers(THRESHOLD - 10, THRESHOLD + 10, THRESHOLD + 10));

            List<AlertEvent> events = detector.detect("R1", new PopView.Pair(cur, prev), NOW);

            assertThat(events).hasSize(2);
            assertThat(extractEffectiveTime(events.get(0))).isEqualTo(SERIES_START_TIME.plusHours(1));
            assertThat(extractEffectiveTime(events.get(1))).isEqualTo(SERIES_START_TIME.plusHours(2));
        }
    }

    @Nested
    @DisplayName("비감지 케이스")
    class NonOnset {

        @Test
        @DisplayName("이전/현재 모두 >= threshold -> 이미 비, onset 아님")
        void bothAbove() {
            PopView prev = buildPopView(ANNOUNCE_TIME.minusHours(3), SERIES_START_TIME,
                    toIntegers(THRESHOLD, THRESHOLD, THRESHOLD));
            PopView cur = buildPopView(ANNOUNCE_TIME, SERIES_START_TIME,
                    toIntegers(THRESHOLD, THRESHOLD + 10, THRESHOLD + 10));

            assertThat(detector.detect("R1", new PopView.Pair(cur, prev), NOW)).isEmpty();
        }

        @Test
        @DisplayName("이전 >= threshold, 현재 < threshold -> 강수 소멸, onset 아님")
        void prevAbove_curBelow() {
            PopView prev = buildPopView(ANNOUNCE_TIME.minusHours(3), SERIES_START_TIME,
                    toIntegers(THRESHOLD, THRESHOLD, THRESHOLD + 10));
            PopView cur = buildPopView(ANNOUNCE_TIME, SERIES_START_TIME,
                    toIntegers(THRESHOLD - 10, 0, 0));

            assertThat(detector.detect("R1", new PopView.Pair(cur, prev), NOW)).isEmpty();
        }
    }

    @Nested
    @DisplayName("특수 입력 처리")
    class SpecialInput {

        @Test
        @DisplayName("현재 POP null -> 해당 포인트 스킵")
        void nullPop_skipped() {
            PopView prev = buildPopView(ANNOUNCE_TIME.minusHours(3), SERIES_START_TIME,
                    toIntegers(THRESHOLD - 10, THRESHOLD - 10));
            PopView cur = buildPopView(ANNOUNCE_TIME, SERIES_START_TIME,
                    new Integer[]{null, THRESHOLD});

            List<AlertEvent> events = detector.detect("R1", new PopView.Pair(cur, prev), NOW);

            assertThat(events).hasSize(1);
            assertThat(extractEffectiveTime(events.get(0))).isEqualTo(SERIES_START_TIME.plusHours(1));
        }

        @Test
        @DisplayName("effectiveTime null -> 스킵 후 이후 포인트 계속 처리")
        void nullEffectiveTime_skipped() {
            List<Hourly.Pop> pops = new ArrayList<>();
            pops.add(new Hourly.Pop(SERIES_START_TIME, THRESHOLD + 10));
            pops.add(new Hourly.Pop(null, THRESHOLD + 20));
            pops.add(new Hourly.Pop(SERIES_START_TIME.plusHours(2), THRESHOLD + 10));
            for (int i = 3; i < MAX_POINTS; i++) {
                pops.add(new Hourly.Pop(SERIES_START_TIME.plusHours(i), null));
            }

            PopView cur = new PopView(new Hourly(pops), buildEmptyDailySeries(), ANNOUNCE_TIME);
            PopView prev = buildPopView(ANNOUNCE_TIME.minusHours(3), SERIES_START_TIME,
                    toIntegers(THRESHOLD - 10, THRESHOLD - 10, THRESHOLD - 10));

            List<AlertEvent> events = detector.detect("R1", new PopView.Pair(cur, prev), NOW);

            assertThat(events).hasSize(2);
            assertThat(extractEffectiveTime(events.get(0))).isEqualTo(SERIES_START_TIME);
            assertThat(extractEffectiveTime(events.get(1))).isEqualTo(SERIES_START_TIME.plusHours(2));
        }

        @Test
        @DisplayName("maxHourlyPoints 초과 시 절단")
        void maxPoints_truncated() {
            RainOnsetDetector smallDetector = new RainOnsetDetector(THRESHOLD, 3);

            PopView prev = buildPopView(ANNOUNCE_TIME.minusHours(3), SERIES_START_TIME,
                    toIntegers(0, 0, THRESHOLD - 10, THRESHOLD - 10));
            PopView cur = buildPopView(ANNOUNCE_TIME, SERIES_START_TIME,
                    toIntegers(THRESHOLD, THRESHOLD, THRESHOLD + 10, THRESHOLD + 10));

            List<AlertEvent> events = smallDetector.detect("R1", new PopView.Pair(cur, prev), NOW);

            assertThat(events).hasSize(3);
        }
    }

    @Nested
    @DisplayName("occurredAt 계산")
    class OccurredAt {

        @Test
        @DisplayName("announceTime 존재 -> occurredAt으로 사용")
        void announceTimePresent() {
            LocalDateTime reportTime = LocalDateTime.of(2026, 1, 22, 5, 30);
            PopView prev = buildPopView(ANNOUNCE_TIME.minusHours(3), SERIES_START_TIME,
                    toIntegers(THRESHOLD - 10));
            PopView cur = buildPopView(reportTime, SERIES_START_TIME,
                    toIntegers(THRESHOLD));

            List<AlertEvent> events = detector.detect("R1", new PopView.Pair(cur, prev), NOW);

            assertThat(events).hasSize(1);
            assertThat(events.get(0).occurredAt()).isEqualTo(reportTime);
        }

        @Test
        @DisplayName("announceTime null -> now 사용")
        void announceTimeNull() {
            PopView prev = buildPopView(null, SERIES_START_TIME, toIntegers(THRESHOLD - 10));
            PopView cur = buildPopView(null, SERIES_START_TIME, toIntegers(THRESHOLD));

            List<AlertEvent> events = detector.detect("R1", new PopView.Pair(cur, prev), NOW);

            assertThat(events).hasSize(1);
            assertThat(events.get(0).occurredAt()).isEqualTo(NOW);
        }
    }

    // ==================== helper ====================

    private LocalDateTime extractEffectiveTime(AlertEvent event) {
        return ((RainOnsetPayload) event.payload()).effectiveTime();
    }

    private Integer[] toIntegers(int... values) {
        Integer[] result = new Integer[values.length];
        for (int i = 0; i < values.length; i++) result[i] = values[i];
        return result;
    }

    private PopView buildPopView(LocalDateTime reportTime,
                                 LocalDateTime seriesStart,
                                 Integer[] pops) {
        List<Hourly.Pop> hourlyPops = new ArrayList<>(MAX_POINTS);
        for (int i = 0; i < MAX_POINTS; i++) {
            Integer pop = (i < pops.length) ? pops[i] : null;
            hourlyPops.add(new Hourly.Pop(seriesStart.plusHours(i), pop));
        }
        return new PopView(new Hourly(hourlyPops), buildEmptyDailySeries(), reportTime);
    }

    private Daily buildEmptyDailySeries() {
        List<Daily.Pop> days = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) days.add(new Daily.Pop(null, null));
        return new Daily(days);
    }
}