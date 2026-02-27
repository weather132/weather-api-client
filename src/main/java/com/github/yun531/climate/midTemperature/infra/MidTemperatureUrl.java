package com.github.yun531.climate.midTemperature.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "url.mid")
public class MidTemperatureUrl {
    private final String temperature;

    public MidTemperatureUrl(String temperature) {
        this.temperature = temperature;
    }

    public String getMidTemperatureUrl() {
        return temperature;
    }
}
