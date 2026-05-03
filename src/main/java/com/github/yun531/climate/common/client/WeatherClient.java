package com.github.yun531.climate.common.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@Component
public class WeatherClient {
    private final RestClient restClient;

    public WeatherClient() {
        this.restClient = RestClient.create();
    }

    public String requestGet(String url, Map<String, String> variables) {
        long startNanos = System.nanoTime();
        try {
            return restClient.get().uri(url, variables).retrieve().body(String.class);
        } catch (RestClientException e) {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.error("GET 실패. url={} elapsedMs={}", url, elapsedMs, e);
            throw e;
        }
    }
}