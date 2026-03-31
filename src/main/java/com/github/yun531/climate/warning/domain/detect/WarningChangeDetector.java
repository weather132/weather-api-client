package com.github.yun531.climate.warning.domain.detect;

import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import com.github.yun531.climate.warning.domain.model.WarningEvent;
import com.github.yun531.climate.warning.domain.model.WarningEventType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WarningChangeDetector {

    public List<WarningEvent> detect(List<WarningCurrent> previousWarnings,
                                     List<WarningCurrent> currentWarnings) {
        Map<String, WarningCurrent> previousMap = toMap(previousWarnings);
        Map<String, WarningCurrent> currentMap = toMap(currentWarnings);

        List<WarningEvent> warningEvents = new ArrayList<>();

        for (var currentEntry : currentMap.entrySet()) {
            WarningCurrent current  = currentEntry.getValue();
            WarningCurrent previous = previousMap.get(currentEntry.getKey());

            if (previous == null) {
                warningEvents.add(current.toEvent(null, WarningEventType.NEW));
            } else {
                classifyChange(previous, current, warningEvents);
            }
        }

        // 해제된 특보 처리
        for (var previousEntry : previousMap.entrySet()) {
            if (!currentMap.containsKey(previousEntry.getKey())) {
                warningEvents.add(previousEntry.getValue().toEvent(null, WarningEventType.LIFTED));
            }
        }

        return warningEvents;
    }

    private void classifyChange(WarningCurrent previous, WarningCurrent current,
                                List<WarningEvent> warningEvents) {
        int levelDiff = current.getLevel().compareTo(previous.getLevel());

        if (levelDiff > 0) {
            warningEvents.add(current.toEvent(previous.getLevel(), WarningEventType.UPGRADED));
        } else if (levelDiff < 0) {
            warningEvents.add(current.toEvent(previous.getLevel(), WarningEventType.DOWNGRADED));
        } else if (!current.getTmFc().equals(previous.getTmFc())) {
            warningEvents.add(current.toEvent(null, WarningEventType.EXTENDED));
        }
    }

    private Map<String, WarningCurrent> toMap(List<WarningCurrent> warnings) {
        return warnings.stream()
                .collect(Collectors.toMap(
                        wc -> wc.getWarningRegionCode() + ":" + wc.getKind().name(),
                        wc -> wc
                ));
    }
}