package com.github.yun531.climate.notification.domain.adjust;

import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.RainOnsetPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RainOnsetAdjusterTest {

    private final RainOnsetAdjuster adjuster = new RainOnsetAdjuster(24, 1);

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 22, 5, 0);

    @Nested
    @DisplayName("null/빈 입력 가드")
    class NullEmptyGuards {

        @Test
        @DisplayName("null 리스트 -> 빈 리스트")
        void nullInput() {
            assertThat(adjuster.adjust(null, NOW, null)).isEmpty();
        }

        @Test
        @DisplayName("빈 리스트 -> 빈 리스트")
        void emptyInput() {
            assertThat(adjuster.adjust(List.of(), NOW, null)).isEmpty();
        }

        @Test
        @DisplayName("null now -> 원본 그대로 반환")
        void nullNow() {
            AlertEvent event = makeEvent(NOW.plusHours(2));

            List<AlertEvent> result = adjuster.adjust(List.of(event), null, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(event);
        }
    }

    @Nested
    @DisplayName("윈도우 필터링")
    class WindowFiltering {

        @Test
        @DisplayName("윈도우 내 이벤트 보존, 밖 이벤트 제거")
        void insideKept_outsideRemoved() {
            AlertEvent inside  = makeEvent(NOW.plusHours(3));
            AlertEvent outside = makeEvent(NOW.plusHours(25));

            List<AlertEvent> result = adjuster.adjust(List.of(inside, outside), NOW, null);

            assertThat(result).hasSize(1);
            assertThat(extractEffectiveTime(result.get(0))).isEqualTo(NOW.plusHours(3));
        }

        @Test
        @DisplayName("now+1 미만 이벤트 제외")
        void beforeWindowStart_excluded() {
            AlertEvent tooSoon = makeEvent(NOW.plusMinutes(30));

            assertThat(adjuster.adjust(List.of(tooSoon), NOW, null)).isEmpty();
        }

        @Test
        @DisplayName("윈도우 경계(start/end) 포함")
        void boundary_inclusive() {
            AlertEvent atStart = makeEvent(NOW.plusHours(1));
            AlertEvent atEnd   = makeEvent(NOW.plusHours(24));

            assertThat(adjuster.adjust(List.of(atStart, atEnd), NOW, null)).hasSize(2);
        }
    }

    @Nested
    @DisplayName("hourLimit")
    class HourLimit {

        @Test
        @DisplayName("hourLimit 적용 -> 윈도우 축소")
        void shrinksWindow() {
            AlertEvent at3h = makeEvent(NOW.plusHours(3));
            AlertEvent at5h = makeEvent(NOW.plusHours(5));

            List<AlertEvent> result = adjuster.adjust(List.of(at3h, at5h), NOW, 4);

            assertThat(result).hasSize(1);
            assertThat(extractEffectiveTime(result.get(0))).isEqualTo(NOW.plusHours(3));
        }

        @Test
        @DisplayName("withinHours < startOffset -> 빈 리스트")
        void lessThanStartOffset_empty() {
            AlertEvent event = makeEvent(NOW.plusHours(2));

            assertThat(adjuster.adjust(List.of(event), NOW, 0)).isEmpty();
        }
    }

    @Nested
    @DisplayName("occurredAt")
    class OccurredAt {

        @Test
        @DisplayName("occurredAt이 now로 통일")
        void normalizedToNow() {
            AlertEvent event = makeEvent(NOW.plusHours(2));

            List<AlertEvent> result = adjuster.adjust(List.of(event), NOW, null);

            assertThat(result.get(0).occurredAt()).isEqualTo(NOW);
        }
    }

    // ==================== helper ====================

    private LocalDateTime extractEffectiveTime(AlertEvent event) {
        return ((RainOnsetPayload) event.payload()).effectiveTime();
    }

    private AlertEvent makeEvent(LocalDateTime effectiveTime) {
        return new AlertEvent(
                AlertTypeEnum.RAIN_ONSET, "R1", NOW,
                new RainOnsetPayload(effectiveTime, 80));
    }
}