package com.github.yun531.climate.forecast.presentation;

import com.github.yun531.climate.common.log.MdcContext;
import com.github.yun531.climate.common.log.TraceIdGenerator;
import com.github.yun531.climate.forecast.application.AirQualityService;
import com.github.yun531.climate.forecast.domain.readmodel.AirQualityView;
import com.github.yun531.climate.forecast.domain.readmodel.RegionAirQualityView;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/forecast")
@RequiredArgsConstructor
public class AirQualityController {

    private final AirQualityService airQualityService;

    @GetMapping("/air-quality")
    @Operation(
            summary = "미세먼지 조회",
            description = "시도 단위 PM10/PM2.5 측정값과 등급 조회, 매시 갱신."
    )
    public ResponseEntity<AirQualityView> getAirQuality(@RequestParam String regionId) {
        try (var ignored = MdcContext.of(Map.of(
                "traceId", TraceIdGenerator.generate(),
                "job", "forecast-air-quality"))) {

            AirQualityView view = airQualityService.getAirQuality(regionId);
            return (view == null)
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(view);
        }
    }

    @GetMapping("/air-quality/batch")
    @Operation(
            summary = "미세먼지 다지역 조회",
            description = "여러 regionId 의 PM10/PM2.5 측정값과 등급을 한 번에 조회. "
    )
    public ResponseEntity<List<RegionAirQualityView>> getAirQualities(
            @RequestParam List<String> regionIds) {
        try (var ignored = MdcContext.of(Map.of(
                "traceId", TraceIdGenerator.generate(),
                "job", "forecast-air-quality-batch"))) {

            return ResponseEntity.ok(airQualityService.getAirQualities(regionIds));
        }
    }
}