package com.github.yun531.climate.notification.domain.detect;

import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.WarningIssuedPayload;
import com.github.yun531.climate.notification.domain.readmodel.WarningView;
import com.github.yun531.climate.warning.domain.model.WarningEventType;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarningIssuedDetectorTest {

    private final WarningIssuedDetector detector = new WarningIssuedDetector();

    private static final LocalDateTime ANNOUNCE = LocalDateTime.of(2026, 1, 22, 10, 0);
    private static final LocalDateTime EFFECTIVE = LocalDateTime.of(2026, 1, 22, 10, 0);

    // ======================= 정상 탐지 =======================

    @Nested
    @DisplayName("정상 탐지")
    class NormalDetection {

        @Test
        @DisplayName("단일 WarningView -> AlertEvent 1건 생성")
        void singleWarning_detected() {
            WarningView rain = new WarningView(
                    1L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                    WarningEventType.NEW, ANNOUNCE, EFFECTIVE);

            List<AlertEvent> events = detector.detect("R1", List.of(rain), null);

            assertThat(events).hasSize(1);
            AlertEvent e = events.get(0);
            assertThat(e.type()).isEqualTo(AlertTypeEnum.WARNING_ISSUED);
            assertThat(e.regionId()).isEqualTo("R1");
        }

        @Test
        @DisplayName("여러 종류 특보 -> 전부 반환")
        void multipleWarnings_allReturned() {
            WarningView rain = new WarningView(
                    1L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                    WarningEventType.NEW, ANNOUNCE, EFFECTIVE);
            WarningView heat = new WarningView(
                    2L, WarningKind.HEAT, WarningLevel.WARNING, null,
                    WarningEventType.NEW, ANNOUNCE, EFFECTIVE);

            List<AlertEvent> events = detector.detect("R1", List.of(rain, heat), null);

            assertThat(events).hasSize(2);
        }

        @Test
        @DisplayName("모든 eventType(NEW, UPGRADED, DOWNGRADED, EXTENDED) 알림 대상")
        void allActiveEventTypes_detected() {
            List<WarningView> warnings = List.of(
                    new WarningView(1L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                            WarningEventType.NEW, ANNOUNCE, EFFECTIVE),
                    new WarningView(2L, WarningKind.HEAT, WarningLevel.WARNING, WarningLevel.ADVISORY,
                            WarningEventType.UPGRADED, ANNOUNCE, EFFECTIVE),
                    new WarningView(3L, WarningKind.WIND, WarningLevel.ADVISORY, WarningLevel.WARNING,
                            WarningEventType.DOWNGRADED, ANNOUNCE, EFFECTIVE),
                    new WarningView(4L, WarningKind.DRY, WarningLevel.ADVISORY, null,
                            WarningEventType.EXTENDED, ANNOUNCE, EFFECTIVE)
            );

            List<AlertEvent> events = detector.detect("R1", warnings, null);

            assertThat(events).hasSize(4);
        }
    }

    // ======================= warningKinds 필터링 =======================

    @Nested
    @DisplayName("warningKinds 필터링")
    class KindFiltering {

        @Test
        @DisplayName("RAIN만 요청하면 HEAT 제외")
        void kindFilter_onlyMatchingKinds() {
            WarningView rain = new WarningView(
                    1L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                    WarningEventType.NEW, ANNOUNCE, EFFECTIVE);
            WarningView heat = new WarningView(
                    2L, WarningKind.HEAT, WarningLevel.WARNING, null,
                    WarningEventType.NEW, ANNOUNCE, EFFECTIVE);

            List<AlertEvent> events = detector.detect(
                    "R1", List.of(rain, heat), EnumSet.of(WarningKind.RAIN));

            assertThat(events).hasSize(1);
            WarningIssuedPayload payload = (WarningIssuedPayload) events.get(0).payload();
            assertThat(payload.kind()).isEqualTo(WarningKind.RAIN);
        }

        @Test
        @DisplayName("빈 EnumSet -> 전체 특보 대상")
        void emptyWarningKinds_includesAll() {
            WarningView rain = new WarningView(
                    1L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                    WarningEventType.NEW, ANNOUNCE, EFFECTIVE);

            List<AlertEvent> events = detector.detect(
                    "R1", List.of(rain), EnumSet.noneOf(WarningKind.class));

            assertThat(events).hasSize(1);
        }

        @Test
        @DisplayName("warningKinds에 없는 종류 요청 -> 빈 결과")
        void warningKindNotInList_empty() {
            WarningView rain = new WarningView(
                    1L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                    WarningEventType.NEW, ANNOUNCE, EFFECTIVE);

            List<AlertEvent> events = detector.detect(
                    "R1", List.of(rain), EnumSet.of(WarningKind.HEAT));

            assertThat(events).isEmpty();
        }
    }

    // ======================= payload 필드 매핑 =======================

    @Nested
    @DisplayName("payload 필드 매핑")
    class PayloadMapping {

        @Test
        @DisplayName("NEW 이벤트 — prevLevel null, eventType NEW")
        void newEvent_payloadFields() {
            WarningView view = new WarningView(
                    42L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                    WarningEventType.NEW, ANNOUNCE, EFFECTIVE);

            List<AlertEvent> events = detector.detect("R1", List.of(view), null);

            WarningIssuedPayload payload = (WarningIssuedPayload) events.get(0).payload();
            assertThat(payload.kind()).isEqualTo(WarningKind.RAIN);
            assertThat(payload.level()).isEqualTo(WarningLevel.ADVISORY);
            assertThat(payload.prevLevel()).isNull();
            assertThat(payload.eventType()).isEqualTo(WarningEventType.NEW);
            assertThat(payload.eventId()).isEqualTo(42L);
            assertThat(payload.effectiveTime()).isEqualTo(EFFECTIVE);
        }

        @Test
        @DisplayName("UPGRADED 이벤트 — prevLevel 존재")
        void upgradedEvent_prevLevelPresent() {
            WarningView view = new WarningView(
                    99L, WarningKind.HEAT, WarningLevel.WARNING, WarningLevel.ADVISORY,
                    WarningEventType.UPGRADED, ANNOUNCE, EFFECTIVE);

            List<AlertEvent> events = detector.detect("R1", List.of(view), null);

            WarningIssuedPayload payload = (WarningIssuedPayload) events.get(0).payload();
            assertThat(payload.level()).isEqualTo(WarningLevel.WARNING);
            assertThat(payload.prevLevel()).isEqualTo(WarningLevel.ADVISORY);
            assertThat(payload.eventType()).isEqualTo(WarningEventType.UPGRADED);
        }
    }

    // ======================= occurredAt 계산 =======================

    @Nested
    @DisplayName("occurredAt 계산")
    class OccurredAt {

        @Test
        @DisplayName("occurredAt은 announceTime을 truncateToMinutes 한 값")
        void occurredAt_basedOnAnnounceTime() {
            LocalDateTime announceTime = LocalDateTime.of(2026, 1, 22, 5, 30, 45);
            WarningView view = new WarningView(
                    1L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                    WarningEventType.NEW, announceTime, EFFECTIVE);

            List<AlertEvent> events = detector.detect("R1", List.of(view), null);

            assertThat(events.get(0).occurredAt())
                    .isEqualTo(LocalDateTime.of(2026, 1, 22, 5, 30));
        }
    }

    // ======================= null/blank 가드 =======================

    @Nested
    @DisplayName("null/blank 가드")
    class NullBlankGuards {

        @Test
        @DisplayName("빈 리스트 -> 빈 결과")
        void emptyList_empty() {
            assertThat(detector.detect("R1", List.of(), null)).isEmpty();
        }

        @Test
        @DisplayName("null warnings -> 빈 결과")
        void nullWarnings_empty() {
            assertThat(detector.detect("R1", null, null)).isEmpty();
        }

        @Test
        @DisplayName("null regionId -> 빈 결과")
        void nullRegion_empty() {
            WarningView view = new WarningView(
                    1L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                    WarningEventType.NEW, ANNOUNCE, EFFECTIVE);

            assertThat(detector.detect(null, List.of(view), null)).isEmpty();
        }

        @Test
        @DisplayName("blank regionId -> 빈 결과")
        void blankRegionId_empty() {
            WarningView view = new WarningView(
                    1L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                    WarningEventType.NEW, ANNOUNCE, EFFECTIVE);

            assertThat(detector.detect("", List.of(view), null)).isEmpty();
            assertThat(detector.detect("   ", List.of(view), null)).isEmpty();
        }
    }
}