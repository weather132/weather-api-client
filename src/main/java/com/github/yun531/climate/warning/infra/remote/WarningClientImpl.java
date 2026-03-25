package com.github.yun531.climate.warning.infra.remote;

import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.common.client.WeatherClient;
import com.github.yun531.climate.warning.domain.WarningClient;
import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WarningClientImpl implements WarningClient {

    private static final DateTimeFormatter TM_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final WeatherClient client;
    private final WarningUrl url;
    private final ApiKey apiKey;
    private final WarningParser parser;

    public WarningClientImpl(WeatherClient client, WarningUrl url,
                             ApiKey apiKey, WarningParser parser) {
        this.client = client;
        this.url = url;
        this.apiKey = apiKey;
        this.parser = parser;
    }

    @Override
    public List<WarningCurrent> requestCurrentWarnings(LocalDateTime tm) {
        String raw = client.requestGet(url.getUrl(), makeParams(tm));
        return parser.parse(raw);
    }

    private Map<String, String> makeParams(LocalDateTime tm) {
        Map<String, String> params = new HashMap<>();
        params.put("fe", "f");
        params.put("tm", tm.format(TM_FORMAT));
        params.put("authKey", apiKey.getApiKey());
        return params;
    }
}