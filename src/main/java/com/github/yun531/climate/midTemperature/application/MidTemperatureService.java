package com.github.yun531.climate.midTemperature.application;

import com.github.yun531.climate.cityRegionCode.reference.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.reference.CityRegionCodeRepository;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureClient;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureDraft;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
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
        MidAnnounceTime announceTime = new MidAnnounceTime(LocalDateTime.now());

        List<MidTemperature> entities = cityRegionCodeRepository.findAll().stream()
                .map(city -> draftsToEntities(city, announceTime,
                        client.requestMidTemperatureDrafts(city.getRegionCode(), announceTime)))
                .flatMap(Collection::stream)
                .toList();

        repository.saveAll(entities);
    }

    private List<MidTemperature> draftsToEntities(CityRegionCode city,
                                                  MidAnnounceTime announceTime,
                                                  List<MidTemperatureDraft> drafts) {
        LocalDateTime at = announceTime.getTime();
        return drafts.stream()
                .map(d -> new MidTemperature(at, d.effectiveTime(), city, d.maxTemp(), d.minTemp()))
                .toList();
    }
}