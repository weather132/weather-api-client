package com.github.yun531.climate.weatherApi;

import com.github.yun531.climate.dto.LandForecast;
import com.github.yun531.climate.dto.TempForecast;
import com.github.yun531.climate.util.WeatherApiUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class WeatherApiClient {
    private final WeatherApiUrls weatherApiUrls;
    private final RestClient restClient;
    private final String apiKey;

    public WeatherApiClient(WeatherApiUrls weatherApiUrls, @Value("${api-key}") String apiKey) {
        this.weatherApiUrls = weatherApiUrls;
        this.apiKey = apiKey;

        this.restClient = RestClient.create();
    }

    // TODO : targetTime 정수로 받도록 수정하기.
    public List<List<Float>> RequestShortTermGridForecast(String targetTime, ForecastCategory forecastVar) throws URISyntaxException {
        String responseBody = restClient.get()
                .uri(new URI(weatherApiUrls.SHORT_GRID_FORECAST))
                .attribute("tmfc", WeatherApiUtil.getShortTermLatestAnnounceTime()) // 발표시간
                .attribute("tmef", targetTime)              // 발효시간
                .attribute("vars", forecastVar)             // 예보변수
                .attribute("authKey", apiKey)               // api 키
                .retrieve()
                .body(String.class);

        return WeatherApiUtil.parseGridData(responseBody);
    }

    public List<TempForecast> requestMidTermTempForecast(String regid) throws URISyntaxException {
        String responseBody = restClient.get()
                .uri(new URI(weatherApiUrls.MID_TEMPERATURE_FORECAST))
                .attribute("pageNo", 1)
                .attribute("numOfRows", 1)
                .attribute("dataType", "JSON")
                .attribute("regid", regid)
                .attribute("tmFc", WeatherApiUtil.getMidTermLatestAnnounceTime())
                .attribute("authKey", apiKey)
                .retrieve()
                .body(String.class);

        return WeatherApiUtil.parseTempForecast(responseBody);
    }

    public List<LandForecast> requestMidTermLandForecast(String regid) throws URISyntaxException {
        String responseBody = restClient.get()
                .uri(new URI(weatherApiUrls.MID_LAND_FORECAST))
                .attribute("pageNo", 1)
                .attribute("numOfRows", 1)
                .attribute("dataType", "JSON")
                .attribute("regid", regid)
                .attribute("tmFc", WeatherApiUtil.getMidTermLatestAnnounceTime())
                .attribute("authKey", apiKey)
                .retrieve()
                .body(String.class);

        return WeatherApiUtil.parseLandForecast(responseBody);
    }
}
