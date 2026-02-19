package com.github.yun531.climate.common.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class WeatherClient {
    private final RestClient restClient;

    public WeatherClient() {
        this.restClient = RestClient.create();
    }

    public String requestGet(String url, Map<String, String> variables) {
        return restClient.get().uri(url, variables).retrieve().body(String.class);
    }
}
