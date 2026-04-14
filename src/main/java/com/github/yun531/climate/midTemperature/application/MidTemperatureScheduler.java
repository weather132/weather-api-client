package com.github.yun531.climate.midTemperature.application;

import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class MidTemperatureScheduler {

    private final MidTemperatureService midTemperatureService;

    public MidTemperatureScheduler(MidTemperatureService midTemperatureService) {
        this.midTemperatureService = midTemperatureService;
    }

    @Scheduled(cron = "0 10 6,18 * * *")
    @Transactional
    public void updateMidTerm() {
        midTemperatureService.updateMidTemperature();
    }
}