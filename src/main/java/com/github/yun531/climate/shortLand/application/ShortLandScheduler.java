package com.github.yun531.climate.shortLand.application;

import com.github.yun531.climate.common.log.MdcContext;
import com.github.yun531.climate.common.log.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Profile("prod")
public class ShortLandScheduler {
    private final ShortLandService shortLandService;

    public ShortLandScheduler(ShortLandService shortLandService) {
        this.shortLandService = shortLandService;
    }

    @Scheduled(cron = "0 10 5,11,17 * * *")
    public void updateShortLand() {
        try (var ignored = MdcContext.of(Map.of(
                "traceId", TraceIdGenerator.generate(),
                "job", "short-land-collect"))) {

            log.info("[ShortLandCollect] 수집 시작");
            long startNanos = System.nanoTime();

            try {
                shortLandService.updateShortland();
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.info("[ShortLandCollect] 수집 완료. elapsedMs={}", elapsedMs);
            } catch (Exception e) {
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.error("[ShortLandCollect] 수집 실패. elapsedMs={}", elapsedMs, e);
            }
        }
    }
}