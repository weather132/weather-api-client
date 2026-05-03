package com.github.yun531.climate.midCollection.application;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.event.MidCollectionRefreshedEvent;
import com.github.yun531.climate.common.log.MdcContext;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandClient;
import com.github.yun531.climate.midLand.domain.MidLandRepository;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureClient;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureRepository;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 중기예보(MidLand + MidTemperature) 통합 수집 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MidCollectionService {

    private final MidLandClient midLandClient;
    private final MidTemperatureClient midTemperatureClient;
    private final MidLandRepository midLandRepository;
    private final MidTemperatureRepository midTemperatureRepository;
    private final ProvinceRegionCodeRepository provinceRegionCodeRepository;
    private final CityRegionCodeRepository cityRegionCodeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void collect() {
        MidAnnounceTime announceTime = new MidAnnounceTime(LocalDateTime.now());

        try (var ignored = MdcContext.of(Map.of(
                "announceTime", announceTime.getTime().toString()))) {

            List<ProvinceRegionCode> provinceCodes = provinceRegionCodeRepository.findAll();
            List<MidLand> midLands = midLandClient.requestMidLands(announceTime, provinceCodes);
            log.info("MidLand 수집 결과: midLands={} provinces={}", midLands.size(), provinceCodes.size());
            midLandRepository.saveAll(midLands);

            List<CityRegionCode> cityCodes = cityRegionCodeRepository.findAll();
            List<MidTemperature> midTemps = midTemperatureClient.requestMidTemperatures(announceTime, cityCodes);
            log.info("MidTemperature 수집 결과: midTemps={} cities={}", midTemps.size(), cityCodes.size());
            midTemperatureRepository.saveAll(midTemps);

            eventPublisher.publishEvent(new MidCollectionRefreshedEvent(announceTime.getTime()));
        }
    }
}