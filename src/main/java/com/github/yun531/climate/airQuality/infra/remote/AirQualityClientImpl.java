package com.github.yun531.climate.airQuality.infra.remote;

import com.github.yun531.climate.airQuality.domain.AirQuality;
import com.github.yun531.climate.airQuality.domain.AirQualityClient;
import com.github.yun531.climate.airQuality.domain.PmItemCode;
import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.common.client.WeatherClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AirQualityClientImpl implements AirQualityClient {

    private final WeatherClient weatherClient;
    private final AirQualityUrl url;
    private final ApiKey apiKey;
    private final AirQualityParser parser;

    public AirQualityClientImpl(WeatherClient weatherClient, AirQualityUrl url,
                                ApiKey apiKey, AirQualityParser parser) {
        this.weatherClient = weatherClient;
        this.url = url;
        this.apiKey = apiKey;
        this.parser = parser;
    }

    @Override
    public List<AirQuality> fetchLatest(PmItemCode itemCode) {
        String raw = requestAirKorea(itemCode);
        return parser.parse(raw, itemCode);
    }

    private String requestAirKorea(PmItemCode itemCode) {
        return weatherClient.requestGet(url.getUrl(), makeParams(itemCode));
    }

    private Map<String, String> makeParams(PmItemCode itemCode) {
        Map<String, String> params = new HashMap<>();
        params.put("serviceKey", apiKey.getAirQualityApiKey());
        params.put("itemCode", itemCode.name());
        return params;
    }
}