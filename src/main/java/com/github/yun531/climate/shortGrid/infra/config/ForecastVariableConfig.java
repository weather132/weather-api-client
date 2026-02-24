package com.github.yun531.climate.shortGrid.infra.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ForecastVariable.class)
public class ForecastVariableConfig {
}
