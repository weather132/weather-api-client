package com.github.yun531.climate.notification.domain.detect;

import com.github.yun531.climate.common.time.TimeUtil;
import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.WarningIssuedPayload;
import com.github.yun531.climate.notification.domain.readmodel.WarningView;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 기상특보 WarningView 목록 -> AlertEvent 목록 변환.
 * warningKinds가 지정되면 해당 종류만 필터링.
 */
public class WarningIssuedDetector {

    public List<AlertEvent> detect(
            String regionId,
            List<WarningView> warningViews,
            @Nullable Set<WarningKind> warningKinds
    ) {
        if (regionId == null || regionId.isBlank()) return List.of();
        if (warningViews == null || warningViews.isEmpty()) return List.of();

        List<AlertEvent> alertEvents = new ArrayList<>(warningViews.size());

        for (WarningView warningView : warningViews) {
            if (matchesKindFilter(warningView, warningKinds)) {
                alertEvents.add(toAlertEvent(regionId, warningView));
            }
        }

        return alertEvents.isEmpty() ? List.of() : List.copyOf(alertEvents);
    }

    private boolean matchesKindFilter(WarningView warningView, @Nullable Set<WarningKind> warningKinds) {
        if (warningKinds == null || warningKinds.isEmpty()) return true;
        return warningKinds.contains(warningView.kind());
    }

    private AlertEvent toAlertEvent(String regionId, WarningView warningView) {
        LocalDateTime occurredAt = TimeUtil.truncateToMinutes(warningView.announceTime());

        WarningIssuedPayload payload = new WarningIssuedPayload(
                warningView.kind(),
                warningView.level(),
                warningView.prevLevel(),
                warningView.eventType(),
                warningView.eventId(),
                warningView.effectiveTime()
        );

        return new AlertEvent(AlertTypeEnum.WARNING_ISSUED, regionId, occurredAt, payload);
    }
}