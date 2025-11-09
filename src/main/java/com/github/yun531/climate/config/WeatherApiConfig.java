package com.github.yun531.climate.config;

import com.github.yun531.climate.weatherApi.WeatherApiClient;
import com.github.yun531.climate.weatherApi.WeatherApiUrls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(WeatherApiUrls.class)
@PropertySource("classpath:weather-api-url.properties")
@PropertySource("classpath:secret-application.properties")
public class WeatherApiConfig {

    @Bean
    public WeatherApiUrls weatherApiUrls(@Autowired WeatherApiUrls weatherApiUrls) {
        System.out.println(weatherApiUrls.SHORT_GRID_FORECAST);
        return weatherApiUrls;
    }

}
