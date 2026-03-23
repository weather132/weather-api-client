package com.github.yun531.climate.notification.application.alert;

import com.github.yun531.climate.notification.domain.adjust.RainForecastAdjuster;
import com.github.yun531.climate.notification.domain.adjust.RainOnsetAdjuster;
import com.github.yun531.climate.notification.domain.detect.RainForecastDetector;
import com.github.yun531.climate.notification.domain.detect.RainOnsetDetector;
import com.github.yun531.climate.notification.domain.readmodel.PopViewReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlertApplicationConfig {

    @Bean
    public GenerateAlertsService generateAlertsService(
            PopViewReader popViewReader,
            RainOnsetDetector rainOnsetDetector,
            RainForecastDetector rainForecastDetector,
            RainOnsetAdjuster onsetAdjuster,
            RainForecastAdjuster forecastAdjuster,
            @Value("${notification.max-region-count:3}") int maxRegionCount,
            @Value("${notification.default-lookback-hours:2}") int defaultLookbackHours
    ) {
        return new GenerateAlertsService(
                popViewReader,
                rainOnsetDetector,
                rainForecastDetector,
                onsetAdjuster,
                forecastAdjuster,
                maxRegionCount,
                defaultLookbackHours
        );
    }
}