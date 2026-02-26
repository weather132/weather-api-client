package com.github.yun531.climate.shortLand.infra.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "url.short.land")
public class ShortLandUrl {
    @Getter
    private final String land;

    public ShortLandUrl(String land) {
        this.land = land;
    }
}
