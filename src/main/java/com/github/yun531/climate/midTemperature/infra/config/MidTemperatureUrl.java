package com.github.yun531.climate.midTemperature.infra.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class MidTemperatureUrl {

    @Value("${url.mid.temperature}")
    private String url;
}