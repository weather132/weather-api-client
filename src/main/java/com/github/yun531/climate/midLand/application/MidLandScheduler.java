package com.github.yun531.climate.midLand.application;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MidLandScheduler {
    private final MidLandService midLandService;

    public MidLandScheduler(MidLandService midLandService) {
        this.midLandService = midLandService;
    }

    @Scheduled(cron = "0 10 6,18 * * *")
    @Transactional
    public void updateMidLands() {
        midLandService.updateMidland();
    }
}
