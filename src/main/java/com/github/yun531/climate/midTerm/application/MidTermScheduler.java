package com.github.yun531.climate.midTerm.application;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MidTermScheduler {

    private final MidTemperatureService midTemperatureService;
    private final MidPopService midPopService;

    public MidTermScheduler(MidTemperatureService midTemperatureService, MidPopService midPopService) {
        this.midTemperatureService = midTemperatureService;
        this.midPopService = midPopService;
    }

    @Scheduled(cron = "0 10 6,18 * * *")
    @Transactional
    public void updateMidTerm() {
        midTemperatureService.updateMidTemperature();
        midPopService.updateMidPop();
    }
}