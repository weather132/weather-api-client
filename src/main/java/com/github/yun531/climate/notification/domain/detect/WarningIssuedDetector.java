package com.github.yun531.climate.notification.domain.detect;

import com.github.yun531.climate.common.time.TimeUtil;
import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.WarningIssuedPayload;
import com.github.yun531.climate.notification.domain.readmodel.WarningView;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 기상특보 WarningView 목록을 AlertEvent 목록으로 변환.
 * warningKinds가 지정되면 해당 종류의 특보만 알림 대상으로 삼는다.
 */
public class WarningIssuedDetector {

    public List<AlertEvent> detect(
            String regionId,
            List<WarningView> warningViews,
            @Nullable Set<String> warningKinds
    ) {
        if (cannotDetect(regionId, warningViews)) {
            return List.of();
        }
        return collectIssuedAlerts(regionId, warningViews, warningKinds);
    }

    private List<AlertEvent> collectIssuedAlerts(
            String regionId,
            List<WarningView> warningViews,
            @Nullable Set<String> warningKinds
    ) {
        List<AlertEvent> warningEvents = new ArrayList<>(warningViews.size());
        for (WarningView warningView : warningViews) {
            if (isTargetKind(warningView, warningKinds)) {
                warningEvents.add(warningAlert(regionId, warningView));
            }
        }
        return List.copyOf(warningEvents);
    }

    private boolean isTargetKind(WarningView warningView, @Nullable Set<String> warningKinds) {
        if (warningKinds == null || warningKinds.isEmpty()) {
            return true;
        }
        return warningKinds.contains(warningView.kind());
    }

    private AlertEvent warningAlert(String regionId, WarningView warningView) {
        LocalDateTime occurredAt = TimeUtil.truncateToMinutes(warningView.announceTime());
        return new AlertEvent(
                AlertTypeEnum.WARNING_ISSUED,
                regionId,
                occurredAt,
                payloadOf(warningView));
    }

    private WarningIssuedPayload payloadOf(WarningView warningView) {
        return new WarningIssuedPayload(
                warningView.kind(),
                warningView.level(),
                warningView.prevLevel(),
                warningView.eventType(),
                warningView.eventId(),
                warningView.effectiveTime());
    }

    private boolean cannotDetect(String regionId, List<WarningView> warningViews) {
        if (regionId == null || regionId.isBlank()) {
            return true;
        }
        return warningViews == null || warningViews.isEmpty();
    }
}