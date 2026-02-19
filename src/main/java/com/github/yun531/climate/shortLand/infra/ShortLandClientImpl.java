package com.github.yun531.climate.shortLand.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.common.client.WeatherClient;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.shortLand.domain.ShortLand;
import com.github.yun531.climate.shortLand.domain.ShortLandClient;
import com.github.yun531.climate.shortLand.infra.config.ShortLandUrl;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Qualifier("short-land-client")
public class ShortLandClientImpl implements ShortLandClient {
    private final ApiKey apiKey;
    private final WeatherClient weatherClient;
    private final ShortLandUrl url;
    private final ParseConfig config;

    public ShortLandClientImpl(ApiKey apiKey, WeatherClient client, ShortLandUrl url, ParseConfig config) {
        this.apiKey = apiKey;
        this.weatherClient = client;
        this.url = url;
        this.config = config;
    }

    @Override
    public List<ShortLand> requestShortLand(CityRegionCode cityRegionCode) {
        Map<String, String> params = makeParameters(cityRegionCode.getRegionCode());
        String json = weatherClient.requestGet(url.getLand(), params);

        return parse(json).stream()
                .map(item -> item.toShortLand(cityRegionCode))
                .toList();
    }

    private Map<String, String> makeParameters(String regionId) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("pageNo", "1");
        parameters.put("numOfRows", "9");
        parameters.put("dataType", "JSON");
        parameters.put("regId", regionId);
        parameters.put("authKey", apiKey.getApiKey());

        return parameters;
    }

    private List<ShortLandItem> parse(String json) {
        return JsonPath.using(config.getConfiguration()).parse(json).read("$.response.body.items.item", new TypeRef<>() {});
    }
}
