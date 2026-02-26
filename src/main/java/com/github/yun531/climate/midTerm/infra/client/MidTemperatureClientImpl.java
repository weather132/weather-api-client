package com.github.yun531.climate.midTerm.infra.client;

import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.common.client.WeatherClient;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.midTerm.domain.MidAnnounceTime;
import com.github.yun531.climate.midTerm.domain.temperature.MidTemperatureClient;
import com.github.yun531.climate.midTerm.domain.temperature.MidTemperatureDraft;
import com.github.yun531.climate.midTerm.infra.client.dto.TempForecastResponseItem;
import com.github.yun531.climate.midTerm.infra.config.MidTermUrls;
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
    private final MidTermUrls urls;
    private final ApiKey apiKey;
    private final ParseConfig parseConfig;

    public MidTemperatureClientImpl(WeatherClient client, MidTermUrls urls, ApiKey apiKey, ParseConfig parseConfig) {
        this.client = client;
        this.urls = urls;
        this.apiKey = apiKey;
        this.parseConfig = parseConfig;
    }

    @Override
    public List<MidTemperatureDraft> requestMidTemperatureDrafts(String regId, MidAnnounceTime announceTime) {
        String json = client.requestGet(urls.MID_TEMPERATURE_FORECAST, makeParams(regId, announceTime));

        TempForecastResponseItem item = JsonPath.using(parseConfig.getConfiguration())
                .parse(json)
                .read("$.response.body.items.item[0]", TempForecastResponseItem.class);

        return item.toDrafts(announceTime.getAnnounceTime());
    }

    private Map<String, String> makeParams(String regId, MidAnnounceTime announceTime) {
        Map<String, String> p = new HashMap<>();
        p.put("pageNo", "1");
        p.put("numOfRows", "1");
        p.put("dataType", "JSON");
        p.put("regId", regId);
        p.put("tmFc", formatToMidTermTime(announceTime.getAnnounceTime()));
        p.put("authKey", apiKey.getApiKey());
        return p;
    }

    private String formatToMidTermTime(LocalDateTime t) {
        return t.format(DateTimeFormatter.ofPattern("yyyyMMddHH00"));
    }
}