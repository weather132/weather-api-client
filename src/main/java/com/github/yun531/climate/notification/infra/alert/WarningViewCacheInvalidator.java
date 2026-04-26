package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.common.event.WarningRefreshedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 특보 수집 완료 이벤트 수신 → WarningView 캐시 무효화.
 */
@Component
@RequiredArgsConstructor
public class WarningViewCacheInvalidator {

    private final WarningViewCacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(WarningRefreshedEvent e) {
        cacheManager.invalidate();
    }
}