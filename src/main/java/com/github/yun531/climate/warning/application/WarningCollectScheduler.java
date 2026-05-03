package com.github.yun531.climate.warning.application;

import com.github.yun531.climate.common.log.MdcContext;
import com.github.yun531.climate.common.log.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class WarningCollectScheduler {

    private final WarningCollectService collectService;

    public WarningCollectScheduler(WarningCollectService collectService) {
        this.collectService = collectService;
    }

    @Scheduled(cron = "0 10 * * * *")
    public void collect() {
        try (var ignored = MdcContext.of(Map.of(
                "traceId", TraceIdGenerator.generate(),
                "job", "warning-collect"))) {

            log.info("[WarningCollect] 수집 시작");
            long startNanos = System.nanoTime();
            LocalDateTime now = LocalDateTime.now();

            try {
                collectService.collect(now);
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.info("[WarningCollect] 수집 완료. elapsedMs={}", elapsedMs);
            } catch (Exception e) {
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.error("[WarningCollect] 수집 실패. elapsedMs={}", elapsedMs, e);
            }
        }
    }
}