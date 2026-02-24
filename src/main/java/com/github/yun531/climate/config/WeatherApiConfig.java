package com.github.yun531.climate.config;

import com.github.yun531.climate.shortGrid.infra.config.ShortGridUrl;
import com.github.yun531.climate.shortLand.infra.config.ShortLandUrl;
import com.github.yun531.climate.weatherApi.WeatherApiUrls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties({
        WeatherApiUrls.class,
        ShortLandUrl.class,
        ShortGridUrl.class,
})
@PropertySource("classpath:weather-api-url.properties")
@PropertySource("classpath:secret-application.properties")
@PropertySource("classpath:short-grid-forecast-variables.properties")
public class WeatherApiConfig {

    @Bean
    public WeatherApiUrls weatherApiUrls(@Autowired WeatherApiUrls weatherApiUrls) {
        return weatherApiUrls;
    }

}
