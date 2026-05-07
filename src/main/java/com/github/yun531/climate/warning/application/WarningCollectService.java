package com.github.yun531.climate.warning.application;

import com.github.yun531.climate.common.event.WarningRefreshedEvent;
import com.github.yun531.climate.common.log.MdcContext;
import com.github.yun531.climate.warning.domain.collect.WarningClient;
import com.github.yun531.climate.warning.domain.collect.WarningChangeDetector;
import com.github.yun531.climate.warning.domain.warningEvent.WarningCurrent;
import com.github.yun531.climate.warning.domain.warningEvent.WarningEvent;
import com.github.yun531.climate.warning.domain.warningEvent.WarningEventRepository;
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
public class WarningCollectService {

    private final WarningClient warningClient;
    private final WarningEventRepository eventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WarningChangeDetector changeDetector = new WarningChangeDetector();

    @Transactional
    public void collect(LocalDateTime tm) {
        try (var ignored = MdcContext.of(Map.of("announceTime", tm.toString()))) {
            List<WarningCurrent> currentWarnings = warningClient.requestCurrentWarnings(tm);
            List<WarningCurrent> activeWarnings  = eventRepository.findActiveWarnings();

            List<WarningEvent> warningEvents = changeDetector.detect(activeWarnings, currentWarnings, tm);

            if (warningEvents.isEmpty()) return;

            eventRepository.saveAll(warningEvents);
            log.info("이벤트 감지: count={} types={}", warningEvents.size(),
                    warningEvents.stream().map(e -> e.getEventType().name()).toList());
            eventPublisher.publishEvent(new WarningRefreshedEvent(tm));
        }
    }
}