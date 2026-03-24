package com.github.yun531.climate.notification.presentation;

import com.github.yun531.climate.notification.application.alert.GenerateAlertsCommand;
import com.github.yun531.climate.notification.application.alert.GenerateAlertsService;
import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.RainOnsetPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean GenerateAlertsService service;
    @Captor ArgumentCaptor<GenerateAlertsCommand> cmdCaptor;

    private static final String BASE_PATH = "/notification/alerts";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 22, 5, 15);


    @Nested
    @DisplayName("GET /rain-onset")
    class RainOnset {

        @Test
        @DisplayName("Command 조립 + 결과 직렬화")
        void assemblesCommand() throws Exception {
            AlertEvent event = new AlertEvent(
                    AlertTypeEnum.RAIN_ONSET, "R1", NOW,
                    new RainOnsetPayload(AlertTypeEnum.RAIN_ONSET, NOW.plusHours(3), 80));
            when(service.generate(cmdCaptor.capture())).thenReturn(List.of(event));

            mvc.perform(get(BASE_PATH + "/rain-onset")
                            .param("regionIds", "R1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].type").value("RAIN_ONSET"))
                    .andExpect(jsonPath("$[0].regionId").value("R1"));

            GenerateAlertsCommand cmd = cmdCaptor.getValue();
            assertThat(cmd.enabledTypes()).containsExactly(AlertTypeEnum.RAIN_ONSET);
            assertThat(cmd.regionIds()).containsExactly("R1");
            assertThat(cmd.withinHours()).isNull();
        }

        @Test
        @DisplayName("withinHours 범위 초과 -> 24로 보정")
        void withinHours_clamped() throws Exception {
            when(service.generate(cmdCaptor.capture())).thenReturn(List.of());

            mvc.perform(get(BASE_PATH + "/rain-onset")
                            .param("regionIds", "R1")
                            .param("withinHours", "99"))
                    .andExpect(status().isOk());

            assertThat(cmdCaptor.getValue().withinHours()).isEqualTo(24);
        }

        @Test
        @DisplayName("regionIds 누락 -> 400")
        void missingRegionIds() throws Exception {
            mvc.perform(get(BASE_PATH + "/rain-onset"))
                    .andExpect(status().isBadRequest());
        }
    }


    @Nested
    @DisplayName("GET /rain-forecast")
    class RainForecast {

        @Test
        @DisplayName("Command 조립 — enabledTypes = RAIN_FORECAST")
        void assemblesCommand() throws Exception {
            when(service.generate(cmdCaptor.capture())).thenReturn(List.of());

            mvc.perform(get(BASE_PATH + "/rain-forecast")
                            .param("regionIds", "R1"))
                    .andExpect(status().isOk());

            GenerateAlertsCommand cmd = cmdCaptor.getValue();
            assertThat(cmd.enabledTypes()).containsExactly(AlertTypeEnum.RAIN_FORECAST);
            assertThat(cmd.regionIds()).containsExactly("R1");
        }
    }


    @Nested
    @DisplayName("GET /summary")
    class Summary {

        @Test
        @DisplayName("Command 조립 — RAIN_ONSET + WARNING_ISSUED 통합")
        void assemblesCommand() throws Exception {
            when(service.generate(cmdCaptor.capture())).thenReturn(List.of());

            mvc.perform(get(BASE_PATH + "/summary")
                            .param("regionIds", "R1", "R2")
                            .param("withinHours", "12"))
                    .andExpect(status().isOk());

            GenerateAlertsCommand cmd = cmdCaptor.getValue();
            assertThat(cmd.enabledTypes())
                    .containsExactlyInAnyOrder(AlertTypeEnum.RAIN_ONSET, AlertTypeEnum.WARNING_ISSUED);
            assertThat(cmd.regionIds()).containsExactly("R1", "R2");
            assertThat(cmd.withinHours()).isEqualTo(12);
        }
    }
}