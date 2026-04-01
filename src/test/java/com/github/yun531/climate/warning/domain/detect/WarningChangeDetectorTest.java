package com.github.yun531.climate.warning.domain.detect;

import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import com.github.yun531.climate.warning.domain.model.WarningEvent;
import com.github.yun531.climate.warning.domain.model.WarningEventType;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarningChangeDetectorTest {

    private final WarningChangeDetector detector = new WarningChangeDetector();

    private static final LocalDateTime PREV_ANNOUNCE_TIME  = LocalDateTime.of(2026, 3, 30, 12, 0);
    private static final LocalDateTime PREV_EFFECTIVE_TIME = LocalDateTime.of(2026, 3, 30, 14, 0);
    private static final LocalDateTime ANNOUNCE_TIME       = LocalDateTime.of(2026, 3, 30, 15, 0);
    private static final LocalDateTime EFFECTIVE_TIME      = LocalDateTime.of(2026, 3, 30, 18, 0);

    @Nested
    @DisplayName("단일 이벤트 감지")
    class SingleEvent {

        @Test
        @DisplayName("신규 발령 - previous 비어있고 current에 1건")
        void newWarning() {
            List<WarningCurrent> previousWarnings = List.of();
            List<WarningCurrent> currentWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.ADVISORY, ANNOUNCE_TIME, EFFECTIVE_TIME)
            );

            List<WarningEvent> warningEvents = detector.detect(previousWarnings, currentWarnings);

            assertThat(warningEvents).hasSize(1);
            assertThat(warningEvents.get(0).getEventType()).isEqualTo(WarningEventType.NEW);
            assertThat(warningEvents.get(0).getWarningRegionCode()).isEqualTo("L1051000");
            assertThat(warningEvents.get(0).getKind()).isEqualTo(WarningKind.RAIN);
            assertThat(warningEvents.get(0).getLevel()).isEqualTo(WarningLevel.ADVISORY);
            assertThat(warningEvents.get(0).getPrevLevel()).isNull();
            assertThat(warningEvents.get(0).getAnnounceTime()).isEqualTo(ANNOUNCE_TIME);
            assertThat(warningEvents.get(0).getEffectiveTime()).isEqualTo(EFFECTIVE_TIME);
        }

        @Test
        @DisplayName("해제 - previous에 1건, current 비어있음")
        void liftedWarning() {
            List<WarningCurrent> previousWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.ADVISORY, PREV_ANNOUNCE_TIME, PREV_EFFECTIVE_TIME)
            );
            List<WarningCurrent> currentWarnings = List.of();

            List<WarningEvent> warningEvents = detector.detect(previousWarnings, currentWarnings);

            assertThat(warningEvents).hasSize(1);
            assertThat(warningEvents.get(0).getEventType()).isEqualTo(WarningEventType.LIFTED);
            assertThat(warningEvents.get(0).getWarningRegionCode()).isEqualTo("L1051000");
            assertThat(warningEvents.get(0).getKind()).isEqualTo(WarningKind.RAIN);
            assertThat(warningEvents.get(0).getPrevLevel()).isNull();
            assertThat(warningEvents.get(0).getAnnounceTime()).isEqualTo(PREV_ANNOUNCE_TIME);
            assertThat(warningEvents.get(0).getEffectiveTime()).isEqualTo(PREV_EFFECTIVE_TIME);
        }

        @Test
        @DisplayName("상향 - ADVISORY 에서 WARNING 으로")
        void upgraded() {
            List<WarningCurrent> previousWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.ADVISORY, PREV_ANNOUNCE_TIME, PREV_EFFECTIVE_TIME)
            );
            List<WarningCurrent> currentWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.WARNING, ANNOUNCE_TIME, EFFECTIVE_TIME)
            );

            List<WarningEvent> warningEvents = detector.detect(previousWarnings, currentWarnings);

            assertThat(warningEvents).hasSize(1);
            assertThat(warningEvents.get(0).getEventType()).isEqualTo(WarningEventType.UPGRADED);
            assertThat(warningEvents.get(0).getLevel()).isEqualTo(WarningLevel.WARNING);
            assertThat(warningEvents.get(0).getPrevLevel()).isEqualTo(WarningLevel.ADVISORY);
            assertThat(warningEvents.get(0).getAnnounceTime()).isEqualTo(ANNOUNCE_TIME);
            assertThat(warningEvents.get(0).getEffectiveTime()).isEqualTo(EFFECTIVE_TIME);
        }

        @Test
        @DisplayName("하향 - WARNING 에서 ADVISORY로")
        void downgraded() {
            List<WarningCurrent> previousWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.WARNING, PREV_ANNOUNCE_TIME, PREV_EFFECTIVE_TIME)
            );
            List<WarningCurrent> currentWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.ADVISORY, ANNOUNCE_TIME, EFFECTIVE_TIME)
            );

            List<WarningEvent> warningEvents = detector.detect(previousWarnings, currentWarnings);

            assertThat(warningEvents).hasSize(1);
            assertThat(warningEvents.get(0).getEventType()).isEqualTo(WarningEventType.DOWNGRADED);
            assertThat(warningEvents.get(0).getLevel()).isEqualTo(WarningLevel.ADVISORY);
            assertThat(warningEvents.get(0).getPrevLevel()).isEqualTo(WarningLevel.WARNING);
            assertThat(warningEvents.get(0).getAnnounceTime()).isEqualTo(ANNOUNCE_TIME);
            assertThat(warningEvents.get(0).getEffectiveTime()).isEqualTo(EFFECTIVE_TIME);
        }

        @Test
        @DisplayName("연장 - 같은 레벨, 새로운 발표시각으로 갱신")
        void extended() {
            List<WarningCurrent> previousWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.ADVISORY, PREV_ANNOUNCE_TIME, PREV_EFFECTIVE_TIME)
            );
            List<WarningCurrent> currentWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.ADVISORY, ANNOUNCE_TIME, EFFECTIVE_TIME)
            );

            List<WarningEvent> warningEvents = detector.detect(previousWarnings, currentWarnings);

            assertThat(warningEvents).hasSize(1);
            assertThat(warningEvents.get(0).getEventType()).isEqualTo(WarningEventType.EXTENDED);
            assertThat(warningEvents.get(0).getLevel()).isEqualTo(WarningLevel.ADVISORY);
            assertThat(warningEvents.get(0).getPrevLevel()).isNull();
            assertThat(warningEvents.get(0).getAnnounceTime()).isEqualTo(ANNOUNCE_TIME);
            assertThat(warningEvents.get(0).getEffectiveTime()).isEqualTo(EFFECTIVE_TIME);
        }

        @Test
        @DisplayName("변화 없음 - 동일 레벨, 동일 발표시각")
        void noChange() {
            List<WarningCurrent> previousWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.ADVISORY, ANNOUNCE_TIME, EFFECTIVE_TIME)
            );
            List<WarningCurrent> currentWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.ADVISORY, ANNOUNCE_TIME, EFFECTIVE_TIME)
            );

            List<WarningEvent> warningEvents = detector.detect(previousWarnings, currentWarnings);

            assertThat(warningEvents).isEmpty();
        }
    }

    @Nested
    @DisplayName("복합 시나리오")
    class CompositeScenario {

        @Test
        @DisplayName("NEW + LIFTED + UPGRADED 동시 발생")
        void multipleEventTypes() {
            List<WarningCurrent> previousWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.ADVISORY, PREV_ANNOUNCE_TIME, PREV_EFFECTIVE_TIME),
                    new WarningCurrent("L1052300", WarningKind.HEAT, WarningLevel.WARNING, PREV_ANNOUNCE_TIME, PREV_EFFECTIVE_TIME)
            );
            List<WarningCurrent> currentWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.WARNING, ANNOUNCE_TIME, EFFECTIVE_TIME),
                    new WarningCurrent("L1090800", WarningKind.WIND, WarningLevel.ADVISORY, ANNOUNCE_TIME, EFFECTIVE_TIME)
            );

            List<WarningEvent> warningEvents = detector.detect(previousWarnings, currentWarnings);

            assertThat(warningEvents).hasSize(3);
            assertThat(warningEvents)
                    .extracting(WarningEvent::getEventType)
                    .containsExactlyInAnyOrder(
                            WarningEventType.UPGRADED,
                            WarningEventType.NEW,
                            WarningEventType.LIFTED
                    );
        }

        @Test
        @DisplayName("같은 지역 다른 kind - 하나는 유지, 하나는 신규")
        void sameRegionDifferentKind() {
            List<WarningCurrent> previousWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.ADVISORY, ANNOUNCE_TIME, EFFECTIVE_TIME)
            );
            List<WarningCurrent> currentWarnings = List.of(
                    new WarningCurrent("L1051000", WarningKind.RAIN, WarningLevel.ADVISORY, ANNOUNCE_TIME, EFFECTIVE_TIME),
                    new WarningCurrent("L1051000", WarningKind.HEAT, WarningLevel.WARNING, ANNOUNCE_TIME, EFFECTIVE_TIME)
            );

            List<WarningEvent> warningEvents = detector.detect(previousWarnings, currentWarnings);

            assertThat(warningEvents).hasSize(1);
            assertThat(warningEvents.get(0).getEventType()).isEqualTo(WarningEventType.NEW);
            assertThat(warningEvents.get(0).getKind()).isEqualTo(WarningKind.HEAT);
        }
    }
}