package com.github.yun531.climate.midLand.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "url.mid")
public class MidLandUrl {
    private final String land;

    public MidLandUrl(String land) {
        this.land = land;
    }

    public String getUrl() {
        return land;
    }
}
