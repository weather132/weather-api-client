package com.github.yun531.climate.midCollection.application;

import com.github.yun531.climate.common.log.MdcContext;
import com.github.yun531.climate.common.log.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 중기예보(MidLand + MidTemperature) 통합 수집 스케줄러.
 */
@Slf4j
@Component
@Profile("!test & !integration-test")
public class MidCollectionScheduler {

    private final MidCollectionService midCollectionService;

    public MidCollectionScheduler(MidCollectionService midCollectionService) {
        this.midCollectionService = midCollectionService;
    }

    @Scheduled(cron = "0 10 6,18 * * *")
    public void collectMidForecast() {
        try (var ignored = MdcContext.of(Map.of(
                "traceId", TraceIdGenerator.generate(),
                "job", "mid-collection-collect"))) {

            log.info("[MidCollectionCollect] 수집 시작");
            long startNanos = System.nanoTime();

            try {
                midCollectionService.collect();
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.info("[MidCollectionCollect] 수집 완료. elapsedMs={}", elapsedMs);
            } catch (Exception e) {
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.error("[MidCollectionCollect] 수집 실패. elapsedMs={}", elapsedMs, e);
            }
        }
    }
}