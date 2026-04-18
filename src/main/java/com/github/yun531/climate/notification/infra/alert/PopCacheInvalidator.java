package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.common.event.MidCollectionRefreshedEvent;
import com.github.yun531.climate.common.event.ShortGridRefreshedEvent;
import com.github.yun531.climate.common.event.ShortLandRefreshedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 수집 완료 이벤트 수신 → POP 캐시 rotate.
 * - rotate: 기존 current → previous로 이동, current 비움.
 */
@Component
@RequiredArgsConstructor
public class PopCacheInvalidator {

    private final PopCacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(ShortGridRefreshedEvent e) {
        cacheManager.rotate();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(ShortLandRefreshedEvent e) {
        cacheManager.rotate();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(MidCollectionRefreshedEvent e) {
        cacheManager.rotate();
    }
}