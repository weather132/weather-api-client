package com.github.yun531.climate.shortLand.application;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.common.event.ShortLandRefreshedEvent;
import com.github.yun531.climate.common.log.MdcContext;
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
import java.util.Map;

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

        if (shortLands.isEmpty()) {
            log.info("수집 결과: shortLands=0");
            return;
        }

        LocalDateTime announceTime = shortLands.get(0).getAnnounceTime();
        try (var ignored = MdcContext.of(Map.of("announceTime", announceTime.toString()))) {
            log.info("수집 결과: shortLands={}", shortLands.size());

            shortLandRepository.saveAll(shortLands);
            eventPublisher.publishEvent(new ShortLandRefreshedEvent(announceTime));
        }
    }
}