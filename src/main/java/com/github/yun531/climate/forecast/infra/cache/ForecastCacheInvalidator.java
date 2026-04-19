package com.github.yun531.climate.forecast.infra.cache;

import com.github.yun531.climate.common.event.MidCollectionRefreshedEvent;
import com.github.yun531.climate.common.event.ShortGridRefreshedEvent;
import com.github.yun531.climate.common.event.ShortLandRefreshedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 수집 완료 이벤트 수신 → forecast 캐시 무효화.
 */
@Component
@RequiredArgsConstructor
public class ForecastCacheInvalidator {

    private final ForecastCacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(ShortGridRefreshedEvent e) {
        cacheManager.invalidateHourly();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(ShortLandRefreshedEvent e) {
        cacheManager.invalidateDaily();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(MidCollectionRefreshedEvent e) {
        cacheManager.invalidateDaily();
    }
}