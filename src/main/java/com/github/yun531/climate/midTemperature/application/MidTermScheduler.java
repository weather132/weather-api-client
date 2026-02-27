package com.github.yun531.climate.midTemperature.application;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MidTermScheduler {

    private final MidTemperatureService midTemperatureService;

    public MidTermScheduler(MidTemperatureService midTemperatureService) {
        this.midTemperatureService = midTemperatureService;
    }

    @Scheduled(cron = "0 10 6,18 * * *")
    @Transactional
    public void updateMidTerm() {
        midTemperatureService.updateMidTemperature();
    }
}