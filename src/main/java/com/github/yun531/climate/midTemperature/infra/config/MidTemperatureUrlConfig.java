package com.github.yun531.climate.midTemperature.infra.config;

import com.github.yun531.climate.midTemperature.infra.MidTemperatureUrl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MidTemperatureUrl.class)
public class MidTemperatureUrlConfig {
}
