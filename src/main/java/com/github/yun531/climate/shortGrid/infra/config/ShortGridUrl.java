package com.github.yun531.climate.shortGrid.infra.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ShortGridUrl {

    @Value("${url.short.grid}")
    private String url;
}