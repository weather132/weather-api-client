package com.github.yun531.climate.notification.domain.detect;

import com.github.yun531.climate.common.time.TimeUtil;
import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.AirPollutionPayload;
import com.github.yun531.climate.notification.domain.readmodel.AirQualityView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 미세먼지 알림 판정.
 * measured view 를 받아 임계 초과 여부를 판정, 초과분만 등급을 매겨 AlertEvent 로 만듦.
 */
@Component
@RequiredArgsConstructor
public class PmAlertDetector {

    private static final String PM10 = "PM10";
    private static final String PM25 = "PM25";

    private final PmAlertThresholds thresholds;

    public List<AlertEvent> detect(String regionId, AirQualityView view) {
        List<AlertEvent> alerts = new ArrayList<>(2);
        addPm10Alert(alerts, view, regionId);
        addPm25Alert(alerts, view, regionId);
        return alerts;
    }

    private void addPm10Alert(List<AlertEvent> alerts, AirQualityView view, String regionId) {
        Integer pm10Value = view.pm10();
        if (pm10Value == null) return;
        if (!thresholds.exceedsAlertPm10(pm10Value)) return;

        alerts.add(buildAlert(PM10, pm10Value, thresholds.gradeOfPm10(pm10Value),
                view.announceTime(), regionId));
    }

    private void addPm25Alert(List<AlertEvent> alerts, AirQualityView view, String regionId) {
        Integer pm25Value = view.pm25();
        if (pm25Value == null) return;
        if (!thresholds.exceedsAlertPm25(pm25Value)) return;

        alerts.add(buildAlert(PM25, pm25Value, thresholds.gradeOfPm25(pm25Value),
                view.announceTime(), regionId));
    }

    private AlertEvent buildAlert(String pollutionType, int value, String grade,
                                  LocalDateTime announceTime, String regionId) {
        AirPollutionPayload payload = new AirPollutionPayload(pollutionType, value, grade, announceTime);
        return new AlertEvent(AlertTypeEnum.AIR_POLLUTION, regionId,
                TimeUtil.truncateToMinutes(announceTime), payload);
    }
}