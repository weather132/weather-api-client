package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.common.event.MidCollectionRefreshedEvent;
import com.github.yun531.climate.common.event.ShortGridRefreshedEvent;
import com.github.yun531.climate.common.event.ShortLandRefreshedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PopCacheInvalidatorTest {

    @Mock PopCacheManager cacheManager;

    private PopCacheInvalidator invalidator;

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 3, 28, 14, 0);

    @BeforeEach
    void setUp() {
        invalidator = new PopCacheInvalidator(cacheManager);
    }

    @Test
    @DisplayName("ShortGridRefreshedEvent → rotate 호출")
    void shortGridEvent_rotates() {
        invalidator.on(new ShortGridRefreshedEvent(ANNOUNCE_TIME));

        verify(cacheManager).rotate();
    }

    @Test
    @DisplayName("ShortLandRefreshedEvent → rotate 호출")
    void shortLandEvent_rotates() {
        invalidator.on(new ShortLandRefreshedEvent(ANNOUNCE_TIME));

        verify(cacheManager).rotate();
    }

    @Test
    @DisplayName("MidCollectionRefreshedEvent → rotate 호출")
    void midCollectionEvent_rotates() {
        invalidator.on(new MidCollectionRefreshedEvent(ANNOUNCE_TIME));

        verify(cacheManager).rotate();
    }
}