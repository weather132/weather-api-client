package com.github.yun531.climate.forecast.infra.cache;

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
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ForecastCacheInvalidatorTest {

    @Mock ForecastCacheManager cacheManager;

    private ForecastCacheInvalidator invalidator;

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 3, 28, 14, 0);

    @BeforeEach
    void setUp() {
        invalidator = new ForecastCacheInvalidator(cacheManager);
    }

    @Test
    @DisplayName("ShortGridRefreshedEvent → invalidateHourly 호출")
    void shortGridEvent_invalidatesHourly() {
        invalidator.on(new ShortGridRefreshedEvent(ANNOUNCE_TIME));

        verify(cacheManager).invalidateHourly();
        verifyNoMoreInteractions(cacheManager);
    }

    @Test
    @DisplayName("ShortLandRefreshedEvent → invalidateDaily 호출")
    void shortLandEvent_invalidatesDaily() {
        invalidator.on(new ShortLandRefreshedEvent(ANNOUNCE_TIME));

        verify(cacheManager).invalidateDaily();
        verifyNoMoreInteractions(cacheManager);
    }

    @Test
    @DisplayName("MidCollectionRefreshedEvent → invalidateDaily 호출")
    void midCollectionEvent_invalidatesDaily() {
        invalidator.on(new MidCollectionRefreshedEvent(ANNOUNCE_TIME));

        verify(cacheManager).invalidateDaily();
        verifyNoMoreInteractions(cacheManager);
    }
}