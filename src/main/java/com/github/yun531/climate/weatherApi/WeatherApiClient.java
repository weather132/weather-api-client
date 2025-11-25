package com.github.yun531.climate.weatherApi;

import com.github.yun531.climate.dto.*;
import com.github.yun531.climate.util.WeatherApiUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
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

    public GridForecast requestShortTermGridForecast(int hoursToTargetTime, String forecastVar) throws URISyntaxException {
        LocalDateTime nowDateTime = LocalDateTime.now();
        LocalDateTime targetTime = nowDateTime.plusHours(hoursToTargetTime);
        LocalDateTime announceTime = WeatherApiUtil.getShortTermLatestAnnounceTime(nowDateTime);

        String responseBody = restClient.get()
                .uri(new URI(weatherApiUrls.SHORT_GRID_FORECAST))
                .attribute("tmfc", WeatherApiUtil.formatToShortTermTime(announceTime)) // 발표시간
                .attribute("tmef", WeatherApiUtil.formatToShortTermTime(targetTime)) // 발효시간
                .attribute("vars", forecastVar) // 예보변수
                .attribute("authKey", apiKey) // api 키
                .retrieve()
                .body(String.class);

        return new GridForecast(
                announceTime,
                targetTime,
                forecastVar,
                WeatherApiUtil.parseGridData(responseBody)
        );
    }

    public List<Temperature> requestMidTermTempForecast(String regionId) throws URISyntaxException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime announceTime = WeatherApiUtil.getMidTermLatestAnnounceTime(now);

        String responseBody = restClient.get()
                .uri(new URI(weatherApiUrls.MID_TEMPERATURE_FORECAST))
                .attribute("pageNo", 1)
                .attribute("numOfRows", 1)
                .attribute("dataType", "JSON")
                .attribute("regid", regionId)
                .attribute("tmFc", announceTime)
                .attribute("authKey", apiKey)
                .retrieve()
                .body(String.class);

        TempForecastResponseItem tempForecastResponseItem = WeatherApiUtil.parseTempForecast(responseBody);
        return tempForecastResponseItem.toTemperatureList(announceTime);
    }

    public List<Pop> requestMidTermLandForecast(String regionId) throws URISyntaxException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime announceTime = WeatherApiUtil.getMidTermLatestAnnounceTime(now);

        String responseBody = restClient.get()
                .uri(new URI(weatherApiUrls.MID_LAND_FORECAST))
                .attribute("pageNo", 1)
                .attribute("numOfRows", 1)
                .attribute("dataType", "JSON")
                .attribute("regid", regionId)
                .attribute("tmFc", announceTime)
                .attribute("authKey", apiKey)
                .retrieve()
                .body(String.class);

        LandForecastResponseItem landForecastResponseItem = WeatherApiUtil.parseLandForecast(responseBody);
        return landForecastResponseItem.toPopList(announceTime);
    }
}
