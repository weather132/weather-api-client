package com.github.yun531.climate.shortLand.application;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.common.event.ShortLandRefreshedEvent;
import com.github.yun531.climate.shortLand.domain.ShortLand;
import com.github.yun531.climate.shortLand.domain.ShortLandClient;
import com.github.yun531.climate.shortLand.domain.ShortLandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLandService {
    private final ShortLandClient client;
    private final CityRegionCodeRepository cityRegionCodeRepository;
    private final ShortLandRepository shortLandRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void updateShortland() {
        List<ShortLand> shortLands = cityRegionCodeRepository.findAll().stream()
                .map(client::requestShortLand)
                .flatMap(List::stream)
                .toList();

        shortLandRepository.saveAll(shortLands);

        eventPublisher.publishEvent(new ShortLandRefreshedEvent(extractAnnounceTime(shortLands)));
    }

    private LocalDateTime extractAnnounceTime(List<ShortLand> shortLands) {
        if (shortLands.isEmpty()) {
            LocalDateTime fallback = LocalDateTime.now();
            log.warn("[ShortLand] 수집 결과가 비어있음. announceTime을 현재 시각으로 폴백: {}", fallback);
            return fallback;
        }

        return shortLands.get(0).getAnnounceTime();
    }
}