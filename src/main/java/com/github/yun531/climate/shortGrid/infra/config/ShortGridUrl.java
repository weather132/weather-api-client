package com.github.yun531.climate.shortGrid.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "url.short.grid")
public class ShortGridUrl {
    private final String grid;

    public ShortGridUrl(String grid) {
        this.grid = grid;
    }

    public String getUrl() {
        return grid;
    }
}
