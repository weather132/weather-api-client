package com.github.yun531.climate.common.apiKey;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKey {
    @Getter
    private final String apiKey;

    public ApiKey(@Value("${api-key}") String apiKey) {
        this.apiKey = apiKey;
    }
}
