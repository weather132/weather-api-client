package com.github.yun531.climate.midTerm.infra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("url.mid")
@RequiredArgsConstructor
public class MidTermUrls {
    public final String temperature;
    public final String land;
}