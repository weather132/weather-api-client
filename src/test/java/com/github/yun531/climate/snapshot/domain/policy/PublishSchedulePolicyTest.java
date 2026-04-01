package com.github.yun531.climate.snapshot.domain.policy;

import com.github.yun531.climate.snapshot.domain.model.SnapKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PublishSchedulePolicyTest {

    private final PublishSchedulePolicy policy = new PublishSchedulePolicy();

    @ParameterizedTest
    @DisplayName("CURRENT: 다양한 시각에서 유효 발표시각 반환")
    @CsvSource({
            // 정상 케이스
            "2026-03-28T14:15, 2026-03-28T14:00",
            "2026-03-28T16:59, 2026-03-28T14:00",
            "2026-03-28T17:10, 2026-03-28T17:00",
            "2026-03-28T08:30, 2026-03-28T08:00",
            "2026-03-28T23:59, 2026-03-28T23:00",
            // 지연 시간 경계 (10분 미만 → 이전 발표시각)
            "2026-03-28T14:05, 2026-03-28T11:00",
            // 정확히 10분 → 해당 발표시각
            "2026-03-28T14:10, 2026-03-28T14:00",
            // 자정 경계
            "2026-03-28T01:00, 2026-03-27T23:00",
    })
    void current_announceTime(String nowStr, String expectedStr) {
        LocalDateTime now      = LocalDateTime.parse(nowStr);
        LocalDateTime expected = LocalDateTime.parse(expectedStr);

        assertThat(policy.announceTimeFor(now, SnapKind.CURRENT)).isEqualTo(expected);
    }

    @Test
    @DisplayName("PREVIOUS = CURRENT - 3시간")
    void previous_is_current_minus_3h() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 28, 14, 15);

        LocalDateTime current  = policy.announceTimeFor(now, SnapKind.CURRENT);
        LocalDateTime previous = policy.announceTimeFor(now, SnapKind.PREVIOUS);

        assertThat(current).isEqualTo(LocalDateTime.of(2026, 3, 28, 14, 0));
        assertThat(previous).isEqualTo(current.minusHours(3));
    }

    @Test
    @DisplayName("자정 경계에서도 PREVIOUS = CURRENT - 3시간")
    void previous_across_midnight() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 28, 2, 15);

        LocalDateTime current  = policy.announceTimeFor(now, SnapKind.CURRENT);
        LocalDateTime previous = policy.announceTimeFor(now, SnapKind.PREVIOUS);

        assertThat(current).isEqualTo(LocalDateTime.of(2026, 3, 28, 2, 0));
        assertThat(previous).isEqualTo(LocalDateTime.of(2026, 3, 27, 23, 0));
    }

    @Test
    @DisplayName("now가 null 이면 null 반환")
    void null_now() {
        assertThat(policy.announceTimeFor(null, SnapKind.CURRENT)).isNull();
    }

    @Test
    @DisplayName("kind가 null 이면 null 반환")
    void null_kind() {
        assertThat(policy.announceTimeFor(LocalDateTime.of(2026, 3, 28, 14, 15), null)).isNull();
    }
}