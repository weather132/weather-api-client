package com.github.yun531.climate.warning.domain.detect;

import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import com.github.yun531.climate.warning.domain.model.WarningEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WarningChangeDetector {

    public List<WarningEvent> detect(List<WarningCurrent> activeWarnings,
                                     List<WarningCurrent> currentWarnings,
                                     LocalDateTime detectedAt) {
        Map<String, WarningCurrent> activeMap  = toMap(activeWarnings);
        Map<String, WarningCurrent> currentMap = toMap(currentWarnings);

        List<WarningEvent> warningEvents = new ArrayList<>();

        for (var currentEntry : currentMap.entrySet()) {
            WarningCurrent current = currentEntry.getValue();
            WarningCurrent active  = activeMap.get(currentEntry.getKey());

            if (active == null) {
                warningEvents.add(WarningEvent.issued(current));
            } else {
                classifyChange(active, current, warningEvents);
            }
        }

        // 해제된 특보 처리
        for (var activeEntry : activeMap.entrySet()) {
            if (!currentMap.containsKey(activeEntry.getKey())) {
                warningEvents.add(WarningEvent.lifted(activeEntry.getValue(), detectedAt));
            }
        }

        return warningEvents;
    }

    private void classifyChange(WarningCurrent active, WarningCurrent current,
                                List<WarningEvent> warningEvents) {
        int levelDiff = current.level().compareTo(active.level());

        if (levelDiff > 0) {
            warningEvents.add(WarningEvent.upgraded(current, active.level()));
        } else if (levelDiff < 0) {
            warningEvents.add(WarningEvent.downgraded(current, active.level()));
        } else if (!current.announceTime().equals(active.announceTime())) {
            warningEvents.add(WarningEvent.extended(current));
        }
    }

    private Map<String, WarningCurrent> toMap(List<WarningCurrent> warnings) {
        return warnings.stream()
                .collect(Collectors.toMap(
                        wc -> wc.warningRegionCode() + ":" + wc.kind().name(),
                        wc -> wc
                ));
    }
}