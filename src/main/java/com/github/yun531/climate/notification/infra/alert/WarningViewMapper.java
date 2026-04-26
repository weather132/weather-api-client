package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.readmodel.WarningView;
import com.github.yun531.climate.warning.contract.IssuedWarning;
import org.springframework.stereotype.Component;

@Component
public class WarningViewMapper {

    public WarningView toWarningView(IssuedWarning w) {
        return new WarningView(
                w.eventId(),
                w.kind().name(),
                w.level().name(),
                w.prevLevel() != null ? w.prevLevel().name() : null,
                w.eventType().name(),
                w.announceTime(),
                w.effectiveTime()
        );
    }
}