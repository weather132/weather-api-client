package com.github.yun531.climate.notification.application.alert;

import com.github.yun531.climate.common.time.TimeUtil;
import com.github.yun531.climate.notification.domain.adjust.RainForecastAdjuster;
import com.github.yun531.climate.notification.domain.adjust.RainOnsetAdjuster;
import com.github.yun531.climate.notification.domain.detect.RainForecastDetector;
import com.github.yun531.climate.notification.domain.detect.RainOnsetDetector;
import com.github.yun531.climate.notification.domain.detect.WarningIssuedDetector;
import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.readmodel.PopView;
import com.github.yun531.climate.notification.domain.readmodel.PopViewReader;
import com.github.yun531.climate.notification.domain.readmodel.WarningView;
import com.github.yun531.climate.notification.domain.readmodel.WarningViewReader;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.*;

import static com.github.yun531.climate.common.time.TimeUtil.nowTruncatedToMinute;

/**
 * 알림 생성 서비스.
 * 흐름: 정규화 -> 타입별 분기 -> Port 로드 -> Detector 감지 -> Adjuster 보정 -> sort
 */
public class GenerateAlertsService {

    private final PopViewReader popViewReader;
    private final WarningViewReader warningViewReader;
    private final RainOnsetDetector rainOnsetDetector;
    private final RainForecastDetector rainForecastDetector;
    private final WarningIssuedDetector warningIssuedDetector;
    private final RainOnsetAdjuster rainOnsetAdjuster;
    private final RainForecastAdjuster rainForecastAdjuster;
    private final int maxRegionCount;

    private static final Comparator<AlertEvent> EVENT_ORDER = Comparator
            .comparing(AlertEvent::type, Comparator.nullsLast(Comparator.comparingInt(Enum::ordinal)))
            .thenComparing(AlertEvent::regionId, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(AlertEvent::occurredAt, Comparator.nullsLast(Comparator.naturalOrder()));

    public GenerateAlertsService(
            PopViewReader popViewReader,
            WarningViewReader warningViewReader,
            RainOnsetDetector rainOnsetDetector,
            RainForecastDetector rainForecastDetector,
            WarningIssuedDetector warningIssuedDetector,
            RainOnsetAdjuster rainOnsetAdjuster,
            RainForecastAdjuster rainForecastAdjuster,
            int maxRegionCount
    ) {
        this.popViewReader = popViewReader;
        this.warningViewReader = warningViewReader;
        this.rainOnsetDetector = rainOnsetDetector;
        this.rainForecastDetector = rainForecastDetector;
        this.warningIssuedDetector = warningIssuedDetector;
        this.rainOnsetAdjuster = rainOnsetAdjuster;
        this.rainForecastAdjuster = rainForecastAdjuster;
        this.maxRegionCount = Math.max(0, maxRegionCount);
    }

    public List<AlertEvent> generate(GenerateAlertsCommand command) {
        return generate(command, nowTruncatedToMinute());
    }

    public List<AlertEvent> generate(GenerateAlertsCommand command, @Nullable LocalDateTime now) {
        if (command == null || command.hasNoTypes()) return List.of();

        Set<AlertTypeEnum> enabledTypes = command.enabledTypes();
        if (enabledTypes == null || enabledTypes.isEmpty()) return List.of();

        LocalDateTime effectiveNow = normalizeNow(now);
        List<String> regionIds = normalizeRegionIds(command.regionIds());
        if (regionIds.isEmpty()) return List.of();

        List<AlertEvent> alertEvents = collectEvents(command, regionIds, effectiveNow);
        if (alertEvents.isEmpty()) return List.of();

        alertEvents.sort(EVENT_ORDER);
        return alertEvents;
    }

    // =====================================================================
    //  타입별 분기 + 지역 순회
    // =====================================================================

    private List<AlertEvent> collectEvents(
            GenerateAlertsCommand cmd, List<String> regionIds, LocalDateTime now
    ) {
        ArrayList<AlertEvent> alertEvents = new ArrayList<>(16);

        for (String regionId : regionIds) {
            if (cmd.isEnabled(AlertTypeEnum.RAIN_ONSET))
                alertEvents.addAll(detectRainOnset(regionId, cmd.withinHours(), now));

            if (cmd.isEnabled(AlertTypeEnum.RAIN_FORECAST))
                alertEvents.addAll(detectRainForecast(regionId, now));

            if (cmd.isEnabled(AlertTypeEnum.WARNING_ISSUED))
                alertEvents.addAll(detectWarningIssued(regionId, cmd.warningKinds()));
        }

        return alertEvents.isEmpty() ? List.of() : alertEvents;
    }

    /** load pair -> detect onset -> adjust(effectiveTime window) */
    private List<AlertEvent> detectRainOnset(
            String regionId, @Nullable Integer withinHours, LocalDateTime now
    ) {
        PopView.Pair pair = popViewReader.loadCurrentPreviousPair(regionId);
        if (pair == null) return List.of();

        List<AlertEvent> raw = rainOnsetDetector.detect(regionId, pair, now);
        if (raw.isEmpty()) return List.of();

        return rainOnsetAdjuster.adjust(raw, now, withinHours);
    }

    /** load current -> detect forecast -> adjust (time shift + clipping) */
    private List<AlertEvent> detectRainForecast(String regionId, LocalDateTime now) {
        PopView view = popViewReader.loadCurrent(regionId);
        if (view == null) return List.of();

        AlertEvent raw = rainForecastDetector.detect(regionId, view, now);
        if (raw == null) return List.of();

        AlertEvent adjusted = rainForecastAdjuster.adjust(raw, raw.occurredAt(), now);
        return (adjusted == null) ? List.of() : List.of(adjusted);
    }

    /** load warning views -> detect issued warnings */
    private List<AlertEvent> detectWarningIssued(
            String regionId, @Nullable Set<String> warningKinds
    ) {
        List<WarningView> warningViews = warningViewReader.loadWarningViews(regionId);
        if (warningViews == null || warningViews.isEmpty()) return List.of();

        return warningIssuedDetector.detect(regionId, warningViews, warningKinds);
    }

    // ============ 정규화 헬퍼 ============

    private LocalDateTime normalizeNow(@Nullable LocalDateTime now) {
        return (now == null) ? nowTruncatedToMinute() : TimeUtil.truncateToMinutes(now);
    }

    private List<String> normalizeRegionIds(@Nullable List<String> regionIds) {
        if (regionIds == null || regionIds.isEmpty()) return List.of();
        if (maxRegionCount == 0) return List.of();

        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String regionId : regionIds) {
            if (regionId == null) continue;

            regionId = regionId.trim();
            if (regionId.isEmpty()) continue;

            set.add(regionId);
            if (set.size() == maxRegionCount) break;
        }
        return List.copyOf(set);
    }

}