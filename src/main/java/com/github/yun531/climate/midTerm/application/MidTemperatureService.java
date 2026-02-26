package com.github.yun531.climate.midTerm.application;

import com.github.yun531.climate.cityRegionCode.reference.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.reference.CityRegionCodeRepository;
import com.github.yun531.climate.midTerm.domain.MidAnnounceTime;
import com.github.yun531.climate.midTerm.domain.temperature.MidTemperature;
import com.github.yun531.climate.midTerm.domain.temperature.MidTemperatureClient;
import com.github.yun531.climate.midTerm.domain.temperature.MidTemperatureDraft;
import com.github.yun531.climate.midTerm.domain.temperature.MidTemperatureRepository;
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
        LocalDateTime at = announceTime.getAnnounceTime();
        return drafts.stream()
                .map(d -> new MidTemperature(at, d.effectiveTime(), city, d.maxTemp(), d.minTemp()))
                .toList();
    }
}