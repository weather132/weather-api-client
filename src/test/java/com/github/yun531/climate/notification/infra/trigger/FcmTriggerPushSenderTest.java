package com.github.yun531.climate.notification.infra.trigger;

import com.github.yun531.climate.fcm.domain.TopicPushMessage;
import com.github.yun531.climate.fcm.domain.TopicPushSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FcmTriggerPushSenderTest {

    @Mock TopicPushSender pushSender;
    @Captor ArgumentCaptor<TopicPushMessage> messageCaptor;

    private FcmTriggerPushSender sender;

    private static final LocalDateTime TRIGGER_TIME = LocalDateTime.of(2026, 1, 22, 8, 5);

    @BeforeEach
    void setUp() {
        TriggerProperties props = new TriggerProperties("hourly", "daily_", 600);
        sender = new FcmTriggerPushSender(pushSender, props);
    }


    @Nested
    @DisplayName("sendHourly")
    class SendHourly {

        @Test
        @DisplayName("메시지 조립 — topic, data, ttl, 반환값")
        void messageAssembly() {
            when(pushSender.push(messageCaptor.capture(), eq(false))).thenReturn("msg-123");

            String result = sender.sendHourly(TRIGGER_TIME, 8, false);

            assertThat(result).isEqualTo("msg-123");
            TopicPushMessage msg = messageCaptor.getValue();
            assertThat(msg.topic()).isEqualTo("hourly");
            assertThat(msg.data())
                    .containsEntry("type", "HOURLY_TRIGGER")
                    .containsEntry("hour", "8")
                    .containsEntry("triggerAtLocal", "2026-01-22T08:05:00");
            assertThat(msg.ttlMillis()).isEqualTo(600_000);
        }

        @Test
        @DisplayName("dryRun 플래그 전달")
        void dryRun() {
            when(pushSender.push(messageCaptor.capture(), eq(true))).thenReturn("dry-001");

            assertThat(sender.sendHourly(TRIGGER_TIME, 8, true)).isEqualTo("dry-001");
        }
    }


    @Nested
    @DisplayName("sendDaily")
    class SendDaily {

        @Test
        @DisplayName("topic은 dailyTopic(hour), type은 DAILY_TRIGGER")
        void messageAssembly() {
            when(pushSender.push(messageCaptor.capture(), anyBoolean())).thenReturn("msg-001");

            sender.sendDaily(TRIGGER_TIME, 8, false);

            TopicPushMessage msg = messageCaptor.getValue();
            assertThat(msg.topic()).isEqualTo("daily_08");
            assertThat(msg.data()).containsEntry("type", "DAILY_TRIGGER");
        }

        @Test
        @DisplayName("hour 범위 밖 -> IllegalArgumentException")
        void invalidHour() {
            assertThatThrownBy(() -> sender.sendDaily(TRIGGER_TIME, -1, false))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> sender.sendDaily(TRIGGER_TIME, 24, false))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}