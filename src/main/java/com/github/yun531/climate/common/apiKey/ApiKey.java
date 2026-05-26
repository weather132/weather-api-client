package com.github.yun531.climate.common.apiKey;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKey {
    @Getter
    private final String apiKey;

    @Getter
    private final String airQualityApiKey;

    public ApiKey(
            @Value("${api-key}") String apiKey,
            @Value("${air-quality.api-key}") String airQualityApiKey
    ) {
        this.apiKey = apiKey;
        this.airQualityApiKey = airQualityApiKey;
    }
}