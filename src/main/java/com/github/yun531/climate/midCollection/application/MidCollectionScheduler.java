package com.github.yun531.climate.midCollection.application;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 중기예보(MidLand + MidTemperature) 통합 수집 스케줄러.
 */
@Component
@Profile("!test & !integration-test")
public class MidCollectionScheduler {

    private final MidCollectionService midCollectionService;

    public MidCollectionScheduler(MidCollectionService midCollectionService) {
        this.midCollectionService = midCollectionService;
    }

    @Scheduled(cron = "0 10 6,18 * * *")
    public void collectMidForecast() {
        midCollectionService.collect();
    }
}