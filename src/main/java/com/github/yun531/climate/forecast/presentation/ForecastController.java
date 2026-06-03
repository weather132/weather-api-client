package com.github.yun531.climate.forecast.presentation;

import com.github.yun531.climate.common.log.MdcContext;
import com.github.yun531.climate.common.log.TraceIdGenerator;
import com.github.yun531.climate.forecast.application.ForecastService;
import com.github.yun531.climate.forecast.domain.readmodel.FcstDailyView;
import com.github.yun531.climate.forecast.domain.readmodel.FcstHourlyView;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;

    @GetMapping("/hourly")
    @Operation(
            summary = "시간대별 예보 조회",
            description = "시간대별(1-24시간) 예보 조회, 3시간 마다 갱신."
    )
    public ResponseEntity<FcstHourlyView> getHourlyForecast(@RequestParam String regionId) {
        try (var ignored = MdcContext.of(Map.of(
                "traceId", TraceIdGenerator.generate(),
                "job", "forecast-hourly"))) {

            FcstHourlyView view = forecastService.getHourlyForecast(regionId);
            return (view == null)
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(view);
        }
    }

    @GetMapping("/daily")
    @Operation(
            summary = "일자별 AM/PM 예보 조회",
            description = "일자별(0-6일차) AM/PM 예보 조회"
    )
    public ResponseEntity<FcstDailyView> getDailyForecast(@RequestParam String regionId) {
        try (var ignored = MdcContext.of(Map.of(
                "traceId", TraceIdGenerator.generate(),
                "job", "forecast-daily"))) {

            FcstDailyView view = forecastService.getDailyForecast(regionId);
            return (view == null)
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(view);
        }
    }
}