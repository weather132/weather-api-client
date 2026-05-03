package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.common.client.WeatherClient;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureClient;
import com.github.yun531.climate.midTemperature.infra.config.MidTemperatureUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
        List<MidTemperature> results = new ArrayList<>();
        int attempted = 0;
        int succeeded = 0;
        int failed = 0;

        for (CityRegionCode regionCode : regionCodes) {
            attempted++;
            try {
                results.addAll(requestAndParse(announceTime, regionCode));
                succeeded++;
            } catch (RestClientException e) {
                // WeatherClient 가 GET 실패 ERROR 로그 + stack trace 출력
                failed++;
            }
        }

        if (failed > 0) {
            log.warn("호출 부분 실패. attempted={} succeeded={} failed={}", attempted, succeeded, failed);
        }

        return results;
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