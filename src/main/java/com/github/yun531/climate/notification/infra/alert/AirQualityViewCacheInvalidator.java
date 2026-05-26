package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.common.event.AirQualityRefreshedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 미세먼지 수집 완료 이벤트 수신 → notification AirQualityView 캐시 무효화.
 */
@Component
@RequiredArgsConstructor
public class AirQualityViewCacheInvalidator {

    private final AirQualityViewCacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(AirQualityRefreshedEvent e) {
        cacheManager.invalidate();
    }
}