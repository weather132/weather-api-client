package com.github.yun531.climate.forecast.application;

import com.github.yun531.climate.forecast.domain.adjust.ForecastWindowAdjuster;
import com.github.yun531.climate.forecast.domain.reader.ForecastViewReader;
import com.github.yun531.climate.forecast.domain.readmodel.AirQualityGradeThresholds;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ForecastApplicationConfig {

    @Bean
    public ForecastWindowAdjuster hourlyForecastWindowAdjuster(
            @Value("${forecast.hourly.max-shift-hours:2}") int maxShiftHours,
            @Value("${forecast.hourly.window-size:24}") int windowSize
    ) {
        return new ForecastWindowAdjuster(maxShiftHours, windowSize);
    }

    @Bean
    public ForecastService forecastService(
            ForecastViewReader viewReader,
            ForecastWindowAdjuster windowAdjuster,
            Clock clock
    ) {
        return new ForecastService(viewReader, windowAdjuster, clock);
    }

    @Bean
    public AirQualityGradeThresholds airQualityGradeThresholds(
            @Value("${air-quality.grade.pm10.good-max}") int pm10GoodMax,
            @Value("${air-quality.grade.pm10.moderate-max}") int pm10ModerateMax,
            @Value("${air-quality.grade.pm10.bad-max}") int pm10BadMax,
            @Value("${air-quality.grade.pm25.good-max}") int pm25GoodMax,
            @Value("${air-quality.grade.pm25.moderate-max}") int pm25ModerateMax,
            @Value("${air-quality.grade.pm25.bad-max}") int pm25BadMax
    ) {
        return new AirQualityGradeThresholds(
                new AirQualityGradeThresholds.Thresholds(pm10GoodMax, pm10ModerateMax, pm10BadMax),
                new AirQualityGradeThresholds.Thresholds(pm25GoodMax, pm25ModerateMax, pm25BadMax)
        );
    }
}