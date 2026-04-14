package com.github.yun531.climate.shortGrid.application;

import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ShortGridScheduler {
    private final ShortGridService shortGridService;

    public ShortGridScheduler(ShortGridService shortGridService) {
        this.shortGridService = shortGridService;
    }

    @Scheduled(cron = "0 10 2/3 * * *")
    @Transactional
    public void updateShortTermGrid() {
        shortGridService.updateShortGrid();
    }
}
