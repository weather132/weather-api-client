package com.github.yun531.climate.shortGrid.infra.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "forecast-variable")
@Getter
public class ForecastVariable {
    private final String pop;
    private final String temperature;

    public ForecastVariable(String pop, String temperature) {
        this.pop = pop;
        this.temperature = temperature;
    }
}
