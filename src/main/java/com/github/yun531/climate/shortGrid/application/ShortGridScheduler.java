package com.github.yun531.climate.shortGrid.application;

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
public class ShortGridScheduler {
    private final ShortGridService shortGridService;

    public ShortGridScheduler(ShortGridService shortGridService) {
        this.shortGridService = shortGridService;
    }

    @Scheduled(cron = "0 10 2/3 * * *")
    public void updateShortTermGrid() {
        try (var ignored = MdcContext.of(Map.of(
                "traceId", TraceIdGenerator.generate(),
                "job", "short-grid-collect"))) {

            log.info("[ShortGridCollect] 수집 시작");
            long startNanos = System.nanoTime();

            try {
                shortGridService.updateShortGrid();
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.info("[ShortGridCollect] 수집 완료. elapsedMs={}", elapsedMs);
            } catch (Exception e) {
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.error("[ShortGridCollect] 수집 실패. elapsedMs={}", elapsedMs, e);
            }
        }
    }
}