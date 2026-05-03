package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.common.client.WeatherClient;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandClient;
import com.github.yun531.climate.midLand.infra.config.MidLandUrl;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
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
public class MidLandClientImpl implements MidLandClient {
    private final WeatherClient weatherClient;
    private final MidLandUrl url;
    private final ApiKey apiKey;
    private final MidLandParser parser;

    public MidLandClientImpl(WeatherClient weatherClient, MidLandUrl url, ApiKey apiKey, MidLandParser parser) {
        this.weatherClient = weatherClient;
        this.url = url;
        this.apiKey = apiKey;
        this.parser = parser;
    }

    @Override
    public List<MidLand> requestMidLands(MidAnnounceTime midAnnounceTime, List<ProvinceRegionCode> regionCodes) {
        List<MidLand> results = new ArrayList<>();
        int attempted = 0;
        int succeeded = 0;
        int failed = 0;

        for (ProvinceRegionCode code : regionCodes) {
            attempted++;
            try {
                results.addAll(requestAndParse(midAnnounceTime, code));
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

    private Map<String, String> makeParams(MidAnnounceTime announceTime, ProvinceRegionCode regionCode) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("pageNo", "1");
        parameters.put("numOfRows", "1");
        parameters.put("dataType", "JSON");
        parameters.put("regId", regionCode.getRegionCode());
        parameters.put("tmFc", format(announceTime.getTime()));
        parameters.put("authKey", apiKey.getApiKey());

        return parameters;
    }

    private String format(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("yyyyMMddHH00"));
    }

    private List<MidLand> requestAndParse(MidAnnounceTime announceTime, ProvinceRegionCode regionCode) {
        String raw = weatherClient.requestGet(url.getUrl(), makeParams(announceTime, regionCode));
        return parser.parse(raw, announceTime, regionCode);
    }


}
