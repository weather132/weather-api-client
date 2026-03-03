package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.cityRegionCode.reference.CityRegionCode;
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
                .map(CityRegionCode::getRegionCode)
                .map(regionCode -> client.requestGet(urls.getMidTemperatureUrl(), makeParams(regionCode, announceTime)))
                .map(raw -> parser.parse(raw, announceTime))
                .flatMap(Collection::stream)
                .toList();
    }


    private Map<String, String> makeParams(String regId, MidAnnounceTime announceTime) {
        Map<String, String> p = new HashMap<>();
        p.put("pageNo", "1");
        p.put("numOfRows", "1");
        p.put("dataType", "JSON");
        p.put("regId", regId);
        p.put("tmFc", formatToMidTermTime(announceTime.getTime()));
        p.put("authKey", apiKey.getApiKey());
        return p;
    }

    private String formatToMidTermTime(LocalDateTime t) {
        return t.format(DateTimeFormatter.ofPattern("yyyyMMddHH00"));
    }
}