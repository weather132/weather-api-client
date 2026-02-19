package com.github.yun531.climate.shortLand.infra.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ShortLandUrl.class)
public class ShortLandUrlConfig {
}
