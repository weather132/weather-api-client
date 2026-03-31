package com.github.yun531.climate.warning.infra.remote;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Getter
@Component
@PropertySource("classpath:weather-api-url.properties")
public class WarningUrl {

    @Value("${url.warning}")
    private String url;
}