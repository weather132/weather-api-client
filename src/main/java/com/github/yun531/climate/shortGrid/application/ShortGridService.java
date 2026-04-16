package com.github.yun531.climate.shortGrid.application;

import com.github.yun531.climate.common.event.ShortGridRefreshedEvent;
import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import com.github.yun531.climate.shortGrid.domain.ShortGridClient;
import com.github.yun531.climate.shortGrid.domain.ShortGridRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShortGridService {
    private final ShortGridRepository shortGridRepository;
    private final ShortGridClient client;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void updateShortGrid() {
        AnnounceTime announceTime = new AnnounceTime(LocalDateTime.now());
        List<ShortGrid> shortGrids = client.requestShortGridsForHours(announceTime, 26);
        shortGridRepository.saveAll(shortGrids);

        eventPublisher.publishEvent(new ShortGridRefreshedEvent(announceTime.getTime()));
    }
}