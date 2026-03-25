package com.github.yun531.climate.warning.application;

import com.github.yun531.climate.warning.domain.WarningClient;
import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import com.github.yun531.climate.warning.domain.model.WarningEvent;
import com.github.yun531.climate.warning.domain.model.WarningEventType;
import com.github.yun531.climate.warning.domain.repository.WarningCurrentRepository;
import com.github.yun531.climate.warning.domain.repository.WarningEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WarningCollectService {

    private final WarningClient warningClient;
    private final WarningCurrentRepository currentRepository;
    private final WarningEventRepository eventRepository;

    public WarningCollectService(WarningClient warningClient,
                                 WarningCurrentRepository currentRepository,
                                 WarningEventRepository eventRepository) {
        this.warningClient = warningClient;
        this.currentRepository = currentRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void collect(LocalDateTime tm) {
        List<WarningCurrent> newSnapshot = warningClient.requestCurrentWarnings(tm);
        List<WarningCurrent> oldSnapshot = currentRepository.findAll();

        List<WarningEvent> events = diff(oldSnapshot, newSnapshot);

        currentRepository.replaceAll(newSnapshot);

        if (!events.isEmpty()) {
            eventRepository.saveAll(events);
            log.info("[WarningCollect] {} 건 이벤트 감지: {}", events.size(),
                    events.stream().map(e -> e.getEventType().name()).toList());
        }
    }

    private List<WarningEvent> diff(List<WarningCurrent> oldSnapshot,
                                    List<WarningCurrent> newSnapshot) {
        Map<String, WarningCurrent> oldMap = toMap(oldSnapshot);
        Map<String, WarningCurrent> newMap = toMap(newSnapshot);

        List<WarningEvent> events = new ArrayList<>();

        for (var entry : newMap.entrySet()) {
            WarningCurrent newWc = entry.getValue();
            WarningCurrent oldWc = oldMap.get(entry.getKey());

            if (oldWc == null) {
                events.add(newWc.toEvent(null, WarningEventType.NEW));
            } else {
                detectChange(oldWc, newWc, events);
            }
        }

        for (var entry : oldMap.entrySet()) {
            if (!newMap.containsKey(entry.getKey())) {
                events.add(entry.getValue().toEvent(null, WarningEventType.LIFTED));
            }
        }

        return events;
    }

    private void detectChange(WarningCurrent oldWc, WarningCurrent newWc,
                              List<WarningEvent> events) {
        int cmp = newWc.getLevel().compareTo(oldWc.getLevel());

        if (cmp > 0) {
            events.add(newWc.toEvent(oldWc.getLevel(), WarningEventType.UPGRADED));
        } else if (cmp < 0) {
            events.add(newWc.toEvent(oldWc.getLevel(), WarningEventType.DOWNGRADED));
        } else if (!newWc.getTmFc().equals(oldWc.getTmFc())) {
            events.add(newWc.toEvent(null, WarningEventType.EXTENDED));
        }
    }

    private Map<String, WarningCurrent> toMap(List<WarningCurrent> snapshot) {
        return snapshot.stream()
                .collect(Collectors.toMap(
                        wc -> wc.getWarningRegionCode() + ":" + wc.getKind().name(),
                        wc -> wc
                ));
    }
}