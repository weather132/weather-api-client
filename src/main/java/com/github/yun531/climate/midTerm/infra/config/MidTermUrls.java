package com.github.yun531.climate.midTerm.infra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("weatherapi")
@RequiredArgsConstructor
public class MidTermUrls {
    public final String MID_TEMPERATURE_FORECAST;
    public final String MID_LAND_FORECAST;
}