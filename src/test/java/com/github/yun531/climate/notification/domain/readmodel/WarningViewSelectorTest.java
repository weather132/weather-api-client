package com.github.yun531.climate.notification.domain.readmodel;

import com.github.yun531.climate.warning.domain.model.WarningEventType;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarningViewSelectorTest {

    private static final LocalDateTime T1 = LocalDateTime.of(2026, 4, 15, 10, 0);
    private static final LocalDateTime T2 = LocalDateTime.of(2026, 4, 16, 10, 0);

    @Nested
    @DisplayName("의미상 중복 통합")
    class Dedup {

        @Test
        @DisplayName("(kind, level) 동일한 4건 -- 1건으로 통합, announceTime 최대값 채택")
        void multipleSameGroup_pickLatest() {
            List<WarningView> raw = List.of(
                    new WarningView(321L, "DRY", "ADVISORY", null,
                            "NEW", T1, T1),
                    new WarningView(325L, "DRY", "ADVISORY", null,
                            "NEW", T1, T1),
                    new WarningView(330L, "DRY", "ADVISORY", null,
                            "NEW", T1, T1),
                    new WarningView(334L, "DRY", "ADVISORY", null,
                            "NEW", T2, T2)
            );

            List<WarningView> result = WarningViewSelector.pickLatestPerKindAndLevel(raw);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).eventId()).isEqualTo(334L);
            assertThat(result.get(0).announceTime()).isEqualTo(T2);
        }

        @Test
        @DisplayName("같은 announceTime 동률 -- 먼저 들어온 항목 유지")
        void sameAnnounceTime_keepsFirst() {
            List<WarningView> raw = List.of(
                    new WarningView(100L, "DRY", "ADVISORY", null,
                            "NEW", T1, T1),
                    new WarningView(200L, "DRY", "ADVISORY", null,
                            "NEW", T1, T1)
            );

            List<WarningView> result = WarningViewSelector.pickLatestPerKindAndLevel(raw);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).eventId()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("보존")
    class Preserve {

        @Test
        @DisplayName("다른 (kind, level)은 모두 보존")
        void differentGroups_allPreserved() {
            List<WarningView> raw = List.of(
                    new WarningView(1L, "RAIN", "WARNING", null,
                            "NEW", T1, T1),
                    new WarningView(2L, "HEAT", "ADVISORY", null,
                            "NEW", T1, T1),
                    new WarningView(3L, "DRY", "ADVISORY", null,
                            "NEW", T1, T1)
            );

            List<WarningView> result = WarningViewSelector.pickLatestPerKindAndLevel(raw);

            assertThat(result).hasSize(3);
            assertThat(result).extracting(WarningView::eventId)
                    .containsExactlyInAnyOrder(1L, 2L, 3L);
        }

        @Test
        @DisplayName("같은 kind 다른 level은 별도 항목으로 유지")
        void sameKindDifferentLevel_separate() {
            List<WarningView> raw = List.of(
                    new WarningView(1L, "RAIN", "ADVISORY", null,
                            "NEW", T1, T1),
                    new WarningView(2L, "RAIN", "WARNING", null,
                            "NEW", T1, T1)
            );

            List<WarningView> result = WarningViewSelector.pickLatestPerKindAndLevel(raw);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(WarningView::level)
                    .containsExactlyInAnyOrder("ADVISORY", "WARNING");
        }
    }

    @Nested
    @DisplayName("경계값")
    class EdgeCase {

        @Test
        @DisplayName("빈 입력 -- 빈 결과")
        void empty_returnsEmpty() {
            assertThat(WarningViewSelector.pickLatestPerKindAndLevel(List.of())).isEmpty();
        }

        @Test
        @DisplayName("null 입력 -- 빈 결과")
        void nullInput_returnsEmpty() {
            assertThat(WarningViewSelector.pickLatestPerKindAndLevel(null)).isEmpty();
        }
    }
}