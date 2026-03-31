package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.readmodel.WarningView;
import com.github.yun531.climate.warning.contract.IssuedWarning;
import org.springframework.stereotype.Component;

@Component
public class WarningViewMapper {

    public WarningView toWarningView(IssuedWarning w) {
        return new WarningView(
                w.eventId(),
                w.kind(),
                w.level(),
                w.prevLevel(),
                w.eventType(),
                w.announceTime(),
                w.effectiveTime()
        );
    }
}