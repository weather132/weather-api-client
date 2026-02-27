package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.common.client.WeatherClient;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandClient;
import com.github.yun531.climate.midLand.infra.config.MidLandUrl;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return regionCodes.stream()
                .map(code -> weatherClient.requestGet(url.getUrl(), makeParams(midAnnounceTime, code)))
                .map(raw -> parser.parse(raw, midAnnounceTime))
                .flatMap(List::stream)
                .toList();
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
}
