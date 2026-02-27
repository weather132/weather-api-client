package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.common.client.WeatherClient;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureClient;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureDraft;
import com.jayway.jsonpath.JsonPath;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MidTemperatureClientImpl implements MidTemperatureClient {

    private final WeatherClient client;
    private final MidTemperatureUrl urls;
    private final ApiKey apiKey;
    private final ParseConfig parseConfig;

    public MidTemperatureClientImpl(WeatherClient client, MidTemperatureUrl urls, ApiKey apiKey, ParseConfig parseConfig) {
        this.client = client;
        this.urls = urls;
        this.apiKey = apiKey;
        this.parseConfig = parseConfig;
    }

    @Override
    public List<MidTemperatureDraft> requestMidTemperatureDrafts(String regId, MidAnnounceTime announceTime) {
        String json = client.requestGet(urls.getMidTemperatureUrl(), makeParams(regId, announceTime));

        TempForecastResponseItem item = JsonPath.using(parseConfig.getConfiguration())
                .parse(json)
                .read("$.response.body.items.item[0]", TempForecastResponseItem.class);

        return item.toDrafts(announceTime.getTime());
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