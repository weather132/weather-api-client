package com.github.yun531.climate.notification.application.alert;

import com.github.yun531.climate.notification.domain.adjust.RainForecastAdjuster;
import com.github.yun531.climate.notification.domain.adjust.RainOnsetAdjuster;
import com.github.yun531.climate.notification.domain.detect.RainForecastDetector;
import com.github.yun531.climate.notification.domain.detect.RainOnsetDetector;
import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload;
import com.github.yun531.climate.notification.domain.payload.RainOnsetPayload;
import com.github.yun531.climate.notification.domain.readmodel.PopView;
import com.github.yun531.climate.notification.domain.readmodel.PopViewReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateAlertsServiceTest {

    @Mock PopViewReader popViewReader;
    @Mock RainOnsetDetector rainOnsetDetector;
    @Mock RainForecastDetector rainForecastDetector;
    @Mock RainOnsetAdjuster rainOnsetAdjuster;
    @Mock RainForecastAdjuster rainForecastAdjuster;

    private GenerateAlertsService service;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 22, 5, 15);

    @BeforeEach
    void setUp() {
        service = new GenerateAlertsService(
                popViewReader,
                rainOnsetDetector, rainForecastDetector,
                rainOnsetAdjuster, rainForecastAdjuster,
                3, 2);
    }


    @Nested
    @DisplayName("입력 가드")
    class InputGuards {

        @Test
        @DisplayName("null command -> 빈 결과")
        void nullCommand() {
            assertThat(service.generate(null, NOW)).isEmpty();
        }

        @Test
        @DisplayName("빈 regionIds -> 빈 결과")
        void emptyRegionIds() {
            var cmd = new GenerateAlertsCommand(
                    List.of(), null, EnumSet.of(AlertTypeEnum.RAIN_ONSET), null);

            assertThat(service.generate(cmd, NOW)).isEmpty();
        }

        @Test
        @DisplayName("빈 enabledTypes -> 빈 결과")
        void emptyEnabledTypes() {
            var cmd = new GenerateAlertsCommand(
                    List.of("R1"), null, EnumSet.noneOf(AlertTypeEnum.class), null);

            assertThat(service.generate(cmd, NOW)).isEmpty();
        }

        @Test
        @DisplayName("maxRegionCount 초과 시 앞에서부터 잘림")
        void regionCount_trimmed() {
            when(popViewReader.loadCurrentPreviousPair(anyString())).thenReturn(mock(PopView.Pair.class));
            when(rainOnsetDetector.detect(anyString(), any(), any())).thenReturn(List.of());

            var cmd = new GenerateAlertsCommand(
                    List.of("R1", "R2", "R3", "R4", "R5"), null,
                    EnumSet.of(AlertTypeEnum.RAIN_ONSET), null);

            service.generate(cmd, NOW);

            verify(popViewReader).loadCurrentPreviousPair("R1");
            verify(popViewReader).loadCurrentPreviousPair("R2");
            verify(popViewReader).loadCurrentPreviousPair("R3");
            verify(popViewReader, never()).loadCurrentPreviousPair("R4");
            verify(popViewReader, never()).loadCurrentPreviousPair("R5");
        }
    }


    @Nested
    @DisplayName("RAIN_ONSET")
    class RainOnset {

        @Test
        @DisplayName("정상 파이프라인 — pair 로드 -> detect -> adjust")
        void fullPipeline() {
            PopView.Pair pair = mock(PopView.Pair.class);
            when(popViewReader.loadCurrentPreviousPair("R1")).thenReturn(pair);

            AlertEvent raw = makeOnsetEvent();
            when(rainOnsetDetector.detect(eq("R1"), eq(pair), any())).thenReturn(List.of(raw));
            when(rainOnsetAdjuster.adjust(anyList(), any(), any())).thenReturn(List.of(raw));

            var cmd = new GenerateAlertsCommand(
                    List.of("R1"), null, EnumSet.of(AlertTypeEnum.RAIN_ONSET), null);

            assertThat(service.generate(cmd, NOW)).hasSize(1);
        }

        @Test
        @DisplayName("pair null -> detect 미호출, 빈 결과")
        void nullPair() {
            when(popViewReader.loadCurrentPreviousPair("R1")).thenReturn(null);

            var cmd = new GenerateAlertsCommand(
                    List.of("R1"), null, EnumSet.of(AlertTypeEnum.RAIN_ONSET), null);

            assertThat(service.generate(cmd, NOW)).isEmpty();
            verify(rainOnsetDetector, never()).detect(any(), any(), any());
        }
    }


    @Nested
    @DisplayName("RAIN_FORECAST")
    class RainForecast {

        @Test
        @DisplayName("정상 파이프라인 — popView 로드 -> detect -> adjust")
        void fullPipeline() {
            PopView view = mock(PopView.class);
            when(popViewReader.loadCurrent("R1")).thenReturn(view);

            AlertEvent raw = makeForecastEvent();
            when(rainForecastDetector.detect(eq("R1"), eq(view), any())).thenReturn(raw);
            when(rainForecastAdjuster.adjust(eq(raw), any(), any())).thenReturn(raw);

            var cmd = new GenerateAlertsCommand(
                    List.of("R1"), null, EnumSet.of(AlertTypeEnum.RAIN_FORECAST), null);

            assertThat(service.generate(cmd, NOW)).hasSize(1);
        }

        @Test
        @DisplayName("popView null -> detect 미호출, 빈 결과")
        void nullView() {
            when(popViewReader.loadCurrent("R1")).thenReturn(null);

            var cmd = new GenerateAlertsCommand(
                    List.of("R1"), null, EnumSet.of(AlertTypeEnum.RAIN_FORECAST), null);

            assertThat(service.generate(cmd, NOW)).isEmpty();
            verify(rainForecastDetector, never()).detect(any(), any(), any());
        }
    }

    // ==================== helper ====================

    private AlertEvent makeOnsetEvent() {
        return new AlertEvent(AlertTypeEnum.RAIN_ONSET, "R1", NOW,
                new RainOnsetPayload(AlertTypeEnum.RAIN_ONSET, NOW.plusHours(3), 80));
    }

    private AlertEvent makeForecastEvent() {
        return new AlertEvent(AlertTypeEnum.RAIN_FORECAST, "R1", NOW,
                new RainForecastPayload(AlertTypeEnum.RAIN_FORECAST, List.of(), List.of()));
    }
}