package com.github.yun531.climate.midLand.infra.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MidLandUrl.class)
public class MidLandUrlConfig {
}
