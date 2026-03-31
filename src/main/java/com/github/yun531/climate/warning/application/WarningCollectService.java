package com.github.yun531.climate.warning.application;

import com.github.yun531.climate.warning.domain.WarningClient;
import com.github.yun531.climate.warning.domain.detect.WarningChangeDetector;
import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import com.github.yun531.climate.warning.domain.model.WarningEvent;
import com.github.yun531.climate.warning.domain.repository.WarningCurrentRepository;
import com.github.yun531.climate.warning.domain.repository.WarningEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class WarningCollectService {

    private final WarningClient warningClient;
    private final WarningCurrentRepository currentRepository;
    private final WarningEventRepository eventRepository;
    private final WarningChangeDetector changeDetector;

    public WarningCollectService(WarningClient warningClient,
                                 WarningCurrentRepository currentRepository,
                                 WarningEventRepository eventRepository) {
        this.warningClient = warningClient;
        this.currentRepository = currentRepository;
        this.eventRepository = eventRepository;
        this.changeDetector = new WarningChangeDetector();
    }

    @Transactional
    public void collect(LocalDateTime tm) {
        List<WarningCurrent> currentWarnings  = warningClient.requestCurrentWarnings(tm);
        List<WarningCurrent> previousWarnings = currentRepository.findAll();

        List<WarningEvent> warningEvents = changeDetector.detect(previousWarnings, currentWarnings);

        currentRepository.replaceAll(currentWarnings);

        if (!warningEvents.isEmpty()) {
            eventRepository.saveAll(warningEvents);
            log.info("[WarningCollect] {} 건 이벤트 감지: {}", warningEvents.size(),
                    warningEvents.stream().map(e -> e.getEventType().name()).toList());
        }
    }
}