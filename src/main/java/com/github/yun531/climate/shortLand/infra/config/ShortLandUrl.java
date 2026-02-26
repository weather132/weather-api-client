package com.github.yun531.climate.shortLand.infra.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "url.short") //todo prefix 수정
public class ShortLandUrl {
    @Getter
    private final String land;

    public ShortLandUrl(String url) {
        this.land = url;
    }
}
