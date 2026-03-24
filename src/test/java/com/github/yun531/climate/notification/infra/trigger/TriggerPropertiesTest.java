package com.github.yun531.climate.notification.infra.trigger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TriggerPropertiesTest {

    private final TriggerProperties props = new TriggerProperties("hourly", "daily_", 600);

    @Test
    @DisplayName("dailyTopic — 한자리 hour은 0-패딩")
    void dailyTopic_singleDigit() {
        assertThat(props.dailyTopic(8)).isEqualTo("daily_08");
    }

    @Test
    @DisplayName("dailyTopic — 두자리 hour은 그대로")
    void dailyTopic_doubleDigit() {
        assertThat(props.dailyTopic(17)).isEqualTo("daily_17");
    }

    @Test
    @DisplayName("ttlMillis — ttlSeconds * 1000 변환")
    void ttlMillis() {
        assertThat(props.ttlMillis()).isEqualTo(600_000);
    }

    @Test
    @DisplayName("ttlSeconds 음수 -> 600 기본값")
    void negativeTtl_defaulted() {
        TriggerProperties negative = new TriggerProperties("h", "d_", -1);

        assertThat(negative.ttlSeconds()).isEqualTo(600);
    }
}
