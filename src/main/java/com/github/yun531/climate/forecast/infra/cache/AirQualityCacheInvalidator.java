package com.github.yun531.climate.forecast.infra.cache;

import com.github.yun531.climate.common.event.AirQualityRefreshedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 미세먼지 수집 완료 이벤트 수신 → forecast AirQualityView 캐시 무효화.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AirQualityCacheInvalidator {

    private final AirQualityCacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(AirQualityRefreshedEvent e) {
        cacheManager.invalidate();
        log.info("AirQualityView 캐시 무효화. announceTime={}", e.announceTime());
    }
}