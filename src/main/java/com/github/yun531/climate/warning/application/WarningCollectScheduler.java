package com.github.yun531.climate.warning.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class WarningCollectScheduler {

    private final WarningCollectService collectService;

    public WarningCollectScheduler(WarningCollectService collectService) {
        this.collectService = collectService;
    }

    @Scheduled(cron = "0 10 * * * *")
    public void collect() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[WarningCollect] 수집 시작: {}", now);

        try {
            collectService.collect(now);
        } catch (Exception e) {
            log.error("[WarningCollect] 수집 실패", e);
        }
    }
}