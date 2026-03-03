package com.github.yun531.climate.midTemperature.application;

import com.github.yun531.climate.cityRegionCode.reference.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.reference.CityRegionCodeRepository;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureClient;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MidTemperatureService {

    private final CityRegionCodeRepository cityRegionCodeRepository;
    private final MidTemperatureClient client;
    private final MidTemperatureRepository repository;

    public MidTemperatureService(CityRegionCodeRepository cityRegionCodeRepository,
                                 MidTemperatureClient client,
                                 MidTemperatureRepository repository) {
        this.cityRegionCodeRepository = cityRegionCodeRepository;
        this.client = client;
        this.repository = repository;
    }

    public void updateMidTemperature() {
        List<CityRegionCode> cityRegionCodes = cityRegionCodeRepository.findAll();
        List<MidTemperature> midTemperatures = client.requestMidTemperatures(new MidAnnounceTime(LocalDateTime.now()), cityRegionCodes);
        repository.saveAll(midTemperatures);
    }


}