package com.github.yun531.climate.shortGrid.application;

import com.github.yun531.climate.common.event.ShortGridRefreshedEvent;
import com.github.yun531.climate.common.log.MdcContext;
import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import com.github.yun531.climate.shortGrid.domain.ShortGridClient;
import com.github.yun531.climate.shortGrid.domain.ShortGridRepository;
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
public class ShortGridService {
    private final ShortGridRepository shortGridRepository;
    private final ShortGridClient client;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void updateShortGrid() {
        AnnounceTime announceTime = new AnnounceTime(LocalDateTime.now());
        int hours = 26;

        try (var ignored = MdcContext.of(Map.of(
                "announceTime", announceTime.getTime().toString()))) {

            List<ShortGrid> shortGrids = client.requestShortGridsForHours(announceTime, hours);
            log.info("수집 결과: shortGrids={} (요청 hours={})", shortGrids.size(), hours);

            shortGridRepository.saveAll(shortGrids);
            eventPublisher.publishEvent(new ShortGridRefreshedEvent(announceTime.getTime()));
        }
    }
}