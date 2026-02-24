package com.github.yun531.climate.weatherApi;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.dto.*;
import com.github.yun531.climate.entity.*;
import com.github.yun531.climate.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class WeatherScheduler {
    private final WeatherApiClient weatherApiClient;
    private final MidPopBatchRepository midPopBatchRepository;
    private final MidTemperatureBatchRepository midTemperatureBatchRepository;
    private final ProvinceRegionCodeRepository provinceRegionCodeRepository;
    private final CityRegionCodeRepository cityRegionCodeRepository;

    @Autowired
    public WeatherScheduler(WeatherApiClient weatherApiClient, MidPopBatchRepository midPopBatchRepository, MidTemperatureBatchRepository midTemperatureBatchRepository, ProvinceRegionCodeRepository provinceRegionCodeRepository, CityRegionCodeRepository cityRegionCodeRepository) {
        this.weatherApiClient = weatherApiClient;
        this.midPopBatchRepository = midPopBatchRepository;
        this.midTemperatureBatchRepository = midTemperatureBatchRepository;
        this.provinceRegionCodeRepository = provinceRegionCodeRepository;
        this.cityRegionCodeRepository = cityRegionCodeRepository;
    }


    @Scheduled(cron = "0 10 6,18 * * *")
    @Transactional
    public void updateMidTerm() {
        updateMidTemperature();
        updateMidPop();
    }


    private void updateMidTemperature() {
        midTemperatureBatchRepository.saveAll(getMidTemps());
    }

    private void updateMidPop() {
        midPopBatchRepository.saveAll(getMidPops());
    }

    private List<MidTemperature> getMidTemps() {
        return cityRegionCodeRepository.findAll().stream()
                .map(code -> weatherApiClient.requestMidTermTempForecast(code.getRegionCode()).stream()
                        .map(temp -> temp.toMidTemperatureEntity(code))
                        .toList())
                .flatMap(Collection::stream)
                .toList();
    }

    private List<MidPop> getMidPops() {
        return provinceRegionCodeRepository.findAll().stream()
                .map(code -> weatherApiClient.requestMidTermLandForecast(code.getRegionCode()).stream()
                        .map(pop -> pop.toMidPopEntity(code))
                        .toList())
                .flatMap(Collection::stream)
                .toList();
    }
}
