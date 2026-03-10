package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.common.client.WeatherClient;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureClient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MidTemperatureClientImpl implements MidTemperatureClient {

    private final WeatherClient client;
    private final MidTemperatureUrl urls;
    private final ApiKey apiKey;
    private final MidTemperatureParser parser;

    public MidTemperatureClientImpl(WeatherClient client, MidTemperatureUrl urls, ApiKey apiKey, MidTemperatureParser parser) {
        this.client = client;
        this.urls = urls;
        this.apiKey = apiKey;
        this.parser = parser;
    }

    @Override
    public List<MidTemperature> requestMidTemperatures(MidAnnounceTime announceTime, List<CityRegionCode> regionCodes) {
        return regionCodes.stream()
                .map(regionCode -> requestAndParse(announceTime, regionCode))
                .flatMap(Collection::stream)
                .toList();
    }


    private Map<String, String> makeParams(MidAnnounceTime announceTime, CityRegionCode cityRegionCode) {
        Map<String, String> params = new HashMap<>();
        params.put("pageNo", "1");
        params.put("numOfRows", "1");
        params.put("dataType", "JSON");
        params.put("regId", cityRegionCode.getRegionCode());
        params.put("tmFc", format(announceTime.getTime()));
        params.put("authKey", apiKey.getApiKey());
        return params;
    }

    private String format(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("yyyyMMddHH00"));
    }

    private List<MidTemperature> requestAndParse(MidAnnounceTime announceTime, CityRegionCode regionCode) {
        String rawJson = client.requestGet(urls.getUrl(), makeParams(announceTime, regionCode));
        return parser.parse(rawJson, announceTime, regionCode);
    }
}