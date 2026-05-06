package com.github.yun531.climate.warning.domain.detect;

import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import com.github.yun531.climate.warning.domain.model.WarningEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class WarningChangeDetector {

    public List<WarningEvent> detect(List<WarningCurrent> activeWarnings,
                                     List<WarningCurrent> currentWarnings,
                                     LocalDateTime detectedAt) {
        Map<String, WarningCurrent> activeMap  = toMap(activeWarnings);
        Map<String, WarningCurrent> currentMap = toMap(currentWarnings);

        List<WarningEvent> warningEvents = new ArrayList<>();
        warningEvents.addAll(detectIssuedOrChanged(activeMap, currentMap));
        warningEvents.addAll(detectLifted(activeMap, currentMap, detectedAt));
        return warningEvents;
    }

    private List<WarningEvent> detectIssuedOrChanged(Map<String, WarningCurrent> activeMap,
                                                     Map<String, WarningCurrent> currentMap) {
        return currentMap.entrySet().stream()
                .map(entry -> classifyEntry(activeMap.get(entry.getKey()), entry.getValue()))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<WarningEvent> classifyEntry(WarningCurrent active, WarningCurrent current) {
        if (active == null) return Optional.of(WarningEvent.issued(current));
        return classifyChange(active, current);
    }

    private Optional<WarningEvent> classifyChange(WarningCurrent active, WarningCurrent current) {
        int levelDiff = current.level().compareTo(active.level());
        if (levelDiff > 0) return Optional.of(WarningEvent.upgraded(current, active.level()));
        if (levelDiff < 0) return Optional.of(WarningEvent.downgraded(current, active.level()));
        if (!current.announceTime().equals(active.announceTime())) return Optional.of(WarningEvent.extended(current));
        return Optional.empty();
    }

    private List<WarningEvent> detectLifted(Map<String, WarningCurrent> activeMap,
                                            Map<String, WarningCurrent> currentMap,
                                            LocalDateTime detectedAt) {
        return activeMap.entrySet().stream()
                .filter(entry -> !currentMap.containsKey(entry.getKey()))
                .map(entry -> WarningEvent.lifted(entry.getValue(), detectedAt))
                .toList();
    }

    private Map<String, WarningCurrent> toMap(List<WarningCurrent> warnings) {
        return warnings.stream()
                .collect(Collectors.toMap(
                        wc -> wc.warningRegionCode() + ":" + wc.kind().name(),
                        wc -> wc
                ));
    }
}