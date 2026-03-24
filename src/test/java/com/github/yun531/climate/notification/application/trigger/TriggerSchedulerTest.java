package com.github.yun531.climate.notification.application.trigger;

import com.github.yun531.climate.fcm.domain.PushFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriggerSchedulerTest {

    @Mock TriggerPushSender sender;

    private TriggerScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TriggerScheduler(sender);
    }

    @Nested
    @DisplayName("triggerHourly")
    class TriggerHourly {

        @Test
        @DisplayName("sender.sendHourly에 위임")
        void delegates() {
            scheduler.triggerHourly();

            verify(sender).sendHourly(any(), anyInt(), anyBoolean());
        }

        @Test
        @DisplayName("sender 예외 시 스케줄러 생존")
        void senderThrows_survives() {
            when(sender.sendHourly(any(), anyInt(), anyBoolean()))
                    .thenThrow(new PushFailedException("FCM error"));

            assertThatNoException().isThrownBy(() -> scheduler.triggerHourly());
        }
    }

    @Nested
    @DisplayName("triggerDaily")
    class TriggerDaily {

        @Test
        @DisplayName("sender.sendDaily에 위임")
        void delegates() {
            scheduler.triggerDaily();

            verify(sender).sendDaily(any(), anyInt(), anyBoolean());
        }

        @Test
        @DisplayName("sender 예외 시 스케줄러 생존")
        void senderThrows_survives() {
            when(sender.sendDaily(any(), anyInt(), anyBoolean()))
                    .thenThrow(new PushFailedException("FCM error"));

            assertThatNoException().isThrownBy(() -> scheduler.triggerDaily());
        }
    }
}