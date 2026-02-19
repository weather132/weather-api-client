package com.github.yun531.climate.shortLand.application;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ShortLandScheduler {
    private final ShortLandService shortLandService;

    public ShortLandScheduler(ShortLandService shortLandService) {
        this.shortLandService = shortLandService;
    }

    @Scheduled(cron = "0 10 5,11,17 * * *")
    @Transactional
    public void updateShortLand() {
        shortLandService.updateShortland();
    }
}
