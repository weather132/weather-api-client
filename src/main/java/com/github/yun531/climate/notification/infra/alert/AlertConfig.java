package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.application.alert.GenerateAlertsService;
import com.github.yun531.climate.notification.domain.detect.PmAlertThresholds;
import com.github.yun531.climate.notification.domain.adjust.RainForecastAdjuster;
import com.github.yun531.climate.notification.domain.adjust.RainOnsetAdjuster;
import com.github.yun531.climate.notification.domain.detect.PmAlertDetector;
import com.github.yun531.climate.notification.domain.detect.RainForecastDetector;
import com.github.yun531.climate.notification.domain.detect.RainOnsetDetector;
import com.github.yun531.climate.notification.domain.detect.WarningIssuedDetector;
import com.github.yun531.climate.notification.domain.readmodel.AirQualityViewReader;
import com.github.yun531.climate.notification.domain.readmodel.PopViewReader;
import com.github.yun531.climate.notification.domain.readmodel.WarningViewReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlertConfig {

    // ---- Detectors ----

    @Bean
    public RainOnsetDetector rainOnsetDetector(
            @Value("${notification.threshold-pop:60}") int thresholdPop,
            @Value("${notification.max-points:26}") int maxHourlyPoints
    ) {
        return new RainOnsetDetector(thresholdPop, maxHourlyPoints);
    }

    @Bean
    public RainForecastDetector rainForecastDetector(
            @Value("${notification.threshold-pop:60}") int thresholdPop,
            @Value("${notification.max-points:26}") int maxHourlyPoints
    ) {
        return new RainForecastDetector(thresholdPop, maxHourlyPoints);
    }

    @Bean
    public WarningIssuedDetector warningIssuedDetector() {
        return new WarningIssuedDetector();
    }

    // ---- Adjusters ----

    @Bean
    public RainOnsetAdjuster rainOnsetAdjuster(
            @Value("${notification.window-hours:24}") int windowHours
    ) {
        return new RainOnsetAdjuster(windowHours);
    }

    @Bean
    public RainForecastAdjuster rainForecastAdjuster(
            @Value("${notification.max-shift-hours:2}") int maxShiftHours,
            @Value("${notification.window-hours:24}") int windowHours
    ) {
        return new RainForecastAdjuster(maxShiftHours, windowHours, 1);
    }

    // ---- Thresholds ----

    @Bean
    public PmAlertThresholds pmAlertThresholds(
            @Value("${air-quality.grade.pm10.moderate-max}") int pm10ModerateMax,
            @Value("${air-quality.grade.pm10.bad-max}") int pm10BadMax,
            @Value("${air-quality.grade.pm25.moderate-max}") int pm25ModerateMax,
            @Value("${air-quality.grade.pm25.bad-max}") int pm25BadMax
    ) {
        return new PmAlertThresholds(
                new PmAlertThresholds.Thresholds(pm10ModerateMax, pm10BadMax),
                new PmAlertThresholds.Thresholds(pm25ModerateMax, pm25BadMax)
        );
    }

    // ---- Service ----

    @Bean
    public GenerateAlertsService generateAlertsService(
            PopViewReader popViewReader,
            WarningViewReader warningViewReader,
            AirQualityViewReader airQualityViewReader,
            RainOnsetDetector rainOnsetDetector,
            RainForecastDetector rainForecastDetector,
            WarningIssuedDetector warningIssuedDetector,
            PmAlertDetector pmAlertDetector,
            RainOnsetAdjuster onsetAdjuster,
            RainForecastAdjuster forecastAdjuster,
            @Value("${notification.max-region-count:3}") int maxRegionCount
    ) {
        return new GenerateAlertsService(
                popViewReader,
                warningViewReader,
                airQualityViewReader,
                rainOnsetDetector,
                rainForecastDetector,
                warningIssuedDetector,
                pmAlertDetector,
                onsetAdjuster,
                forecastAdjuster,
                maxRegionCount
        );
    }
}