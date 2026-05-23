package com.github.yun531.climate.airQuality.application;

import com.github.yun531.climate.common.log.MdcContext;
import com.github.yun531.climate.common.log.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Profile("!test & !integration-test")
public class AirQualityScheduler {

    private final AirQualityCollectService collectService;

    public AirQualityScheduler(AirQualityCollectService collectService) {
        this.collectService = collectService;
    }

    @Scheduled(cron = "0 22 * * * *")
    public void collect() {
        try (var ignored = MdcContext.of(Map.of(
                "traceId", TraceIdGenerator.generate(),
                "job", "air-quality-collect"))) {

            log.info("[AirQualityCollect] 수집 시작");
            long startNanos = System.nanoTime();

            try {
                collectService.collect();
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.info("[AirQualityCollect] 수집 완료. elapsedMs={}", elapsedMs);
            } catch (Exception e) {
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.error("[AirQualityCollect] 수집 실패. elapsedMs={}", elapsedMs, e);
            }
        }
    }
}