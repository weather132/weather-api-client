package com.github.yun531.climate.airQuality.application;

import com.github.yun531.climate.airQuality.domain.AirQuality;
import com.github.yun531.climate.airQuality.domain.AirQualityClient;
import com.github.yun531.climate.airQuality.domain.AirQualityRepository;
import com.github.yun531.climate.airQuality.domain.PmItemCode;
import com.github.yun531.climate.common.event.AirQualityRefreshedEvent;
import com.github.yun531.climate.common.log.MdcContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AirQualityCollectService {

    private final AirQualityClient client;
    private final AirQualityRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void collect() {
        List<AirQuality> measurements = collectBothItems();
        List<AirQuality> newMeasurements = onlyNewer(measurements);

        if (newMeasurements.isEmpty()) {
            log.info("적재할 신규 측정치 없음");
            return;
        }
        saveAndAnnounce(newMeasurements);
    }

    private List<AirQuality> collectBothItems() {
        List<AirQuality> pm10 = collectItem(PmItemCode.PM10);
        List<AirQuality> pm25 = collectItem(PmItemCode.PM25);
        return pairByTimeAndSido(pm10, pm25);
    }

    private List<AirQuality> collectItem(PmItemCode itemCode) {
        try {
            return client.fetchLatest(itemCode);
        } catch (Exception e) {
            log.warn("수집 실패. itemCode={}", itemCode, e);
            return List.of();
        }
    }

    private List<AirQuality> pairByTimeAndSido(List<AirQuality> pm10List, List<AirQuality> pm25List) {
        Map<MeasureKey, Integer> pm10Values = indexBy(pm10List, AirQuality::getPm10);
        Map<MeasureKey, Integer> pm25Values = indexBy(pm25List, AirQuality::getPm25);

        return pm10Values.keySet().stream()
                .filter(pm25Values::containsKey)
                .map(key -> new AirQuality(
                        key.sidoId(), key.announceTime(),
                        pm10Values.get(key), pm25Values.get(key)))
                .toList();
    }

    private Map<MeasureKey, Integer> indexBy(List<AirQuality> measurements,
                                             Function<AirQuality, Integer> valueExtractor) {
        return measurements.stream()
                .collect(Collectors.toMap(
                        m -> new MeasureKey(m.getSidoRegionCodeId(), m.getAnnounceTime()),
                        valueExtractor,
                        (first, second) -> first));
    }

    private List<AirQuality> onlyNewer(List<AirQuality> measurements) {
        LocalDateTime lastSaved = repository.findLatestAnnounceTime().orElse(LocalDateTime.MIN);
        return measurements.stream()
                .filter(m -> m.getAnnounceTime().isAfter(lastSaved))
                .toList();
    }

    private void saveAndAnnounce(List<AirQuality> measurements) {
        LocalDateTime latest = latestAnnounceTimeOf(measurements);

        try (var ignored = MdcContext.of(Map.of("announceTime", latest.toString()))) {
            repository.saveAll(measurements);
            log.info("수집 결과: airQuality={}", measurements.size());
            eventPublisher.publishEvent(new AirQualityRefreshedEvent(latest));
        }
    }

    private LocalDateTime latestAnnounceTimeOf(List<AirQuality> measurements) {
        return measurements.stream()
                .map(AirQuality::getAnnounceTime)
                .max(Comparator.naturalOrder())
                .orElseThrow();
    }

    private record MeasureKey(Long sidoId, LocalDateTime announceTime) {}
}