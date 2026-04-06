package com.github.yun531.climate.midLand.infra.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class MidLandUrl {

    @Value("${url.mid.land}")
    private String url;
}