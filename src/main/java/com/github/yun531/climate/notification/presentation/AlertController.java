package com.github.yun531.climate.notification.presentation;

import com.github.yun531.climate.common.log.MdcContext;
import com.github.yun531.climate.common.log.TraceIdGenerator;
import com.github.yun531.climate.notification.application.alert.GenerateAlertsCommand;
import com.github.yun531.climate.notification.application.alert.GenerateAlertsService;
import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification/alerts")
public class AlertController {

    private final GenerateAlertsService service;

    @GetMapping("/rain-onset")
    @Operation(
            summary = "일기예보 변동사항 알림",
            description = "3시간 마다 발표되는 24시간이내의 일기예보의 변동사항에 대한 알림")
    public ResponseEntity<List<AlertEvent>> get3HourIntervalForecast(
            @RequestParam List<String> regionIds,
            @RequestParam(value = "withinHours", required = false) Integer withinHours) {

        try (var ignored = logger("alerts-rain-onset")) {

            return makeResponse(regionIds, EnumSet.of(AlertTypeEnum.RAIN_ONSET), null, withinHours);
        }
    }

    @GetMapping("/rain-forecast")
    @Operation(
            summary = "일기예보 요약 알림",
            description = "24시간 이내의 비오는 시간대와, 7일이내의 오전/오후 일기예보 알림")
    public ResponseEntity<List<AlertEvent>> getDayForecast(@RequestParam List<String> regionIds) {
        try (var ignored = logger("alerts-rain-forecast")) {

            return makeResponse(regionIds,
                    EnumSet.of(AlertTypeEnum.RAIN_FORECAST),
                    null,
                    null);
        }
    }

    @GetMapping("/warning-issued")
    @Operation(
            summary = "기상특보 변동사항 알림",
            description = "1시간마다 발표되는 기상특보의 변동사항에 대한 알림")
    public ResponseEntity<List<AlertEvent>> getWarning(
            @RequestParam List<String> regionIds,
            @RequestParam(value = "warningKinds", required = false) Set<WarningKind> warningKinds) {

        try (var ignored = logger("alerts-warning-issued")) {

            var cmd = new GenerateAlertsCommand(
                        regionIds,
                        EnumSet.of(AlertTypeEnum.WARNING_ISSUED),
                        toKindCodes(warningKinds),
                        null);

            return makeResponse(regionIds,
                    EnumSet.of(AlertTypeEnum.WARNING_ISSUED),
                    warningKinds,
                    null);
        }
    }

    @GetMapping("/summary")
    @Operation(
            summary = "단기 예보 + 기상특보 통합 알림",
            description = "3시간 단기예보 변동사항(RAIN_ONSET)과 기상특보 변동사항(WARNING_ISSUED)을 통합 조회")
    public ResponseEntity<List<AlertEvent>> getSummary(
            @RequestParam List<String> regionIds,
            @RequestParam(value = "withinHours", required = false) Integer withinHours,
            @RequestParam(value = "warningKinds", required = false) Set<WarningKind> warningKinds) {

        try (var ignored = logger("alerts-summary")) {

            Integer hours = validateWithinHours(withinHours);

            return makeResponse(regionIds, EnumSet.of(AlertTypeEnum.RAIN_ONSET, AlertTypeEnum.WARNING_ISSUED), warningKinds, hours);
        }
    }


    @Nullable
    private Set<String> toKindCodes(@Nullable Set<WarningKind> kinds) {
        return kinds == null ? null : kinds.stream().map(Enum::name).collect(toUnmodifiableSet());
    }

    private Integer validateWithinHours(Integer withinHours) {
        return withinHours != null && isHourOutOfDayRange(withinHours) ? 24 : withinHours;
    }

    private boolean isHourOutOfDayRange(Integer withinHours) {
        return withinHours < 1 || withinHours > 24;
    }

    private MdcContext logger(String v2) {
        return MdcContext.of(Map.of(
                "traceId",
                TraceIdGenerator.generate(),
                "job",
                v2));
    }

    private ResponseEntity<List<AlertEvent>> makeResponse(List<String> regionIds,
                                                          EnumSet<AlertTypeEnum> rainOnset,
                                                          Set<WarningKind> warningKinds,
                                                          Integer hours) {

        var cmd = new GenerateAlertsCommand(
                regionIds,
                rainOnset,
                toKindCodes(warningKinds),
                hours);

        return ResponseEntity.ok(service.generate(cmd));
    }

}