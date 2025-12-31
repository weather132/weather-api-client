package com.github.yun531.climate.weatherApi;

import com.github.yun531.climate.dto.*;
import com.github.yun531.climate.util.WeatherApiUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public GridForecast requestShortTermGridForecast(int hoursToTargetTime, String forecastVar) {
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        LocalDateTime targetTime = now.plusHours(hoursToTargetTime);
        LocalDateTime announceTime = WeatherApiUtil.getShortTermLatestAnnounceTime(now);

        return requestShortTermGridForecastWithTimes(forecastVar, announceTime, targetTime);
    }

    public GridForecast requestShortTermGridForecastAfterDays(int dayAfter, String forecastVar) {
        LocalDateTime now =  LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        LocalDateTime targetTime = now.plusDays(dayAfter).withHour(12);
        LocalDateTime announceTime = WeatherApiUtil.getShortTermLatestAnnounceTime(now);

        return requestShortTermGridForecastWithTimes(forecastVar, announceTime, targetTime);
    }

    public List<ShortLandForecastItem> requestShortTermLandForecast(String regionId) {
        Map<String, String> parameters = getShortLandParameters(regionId);

        String json = requestGet(weatherApiUrls.SHORT_LAND_FORECAST, parameters);

        return WeatherApiUtil.parseShortLandForecast(json);
    }

    public List<Temperature> requestMidTermTempForecast(String regionId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime announceTime = WeatherApiUtil.getMidTermLatestAnnounceTime(now);

        Map<String, String> parameters = getMidTermParameters(regionId, announceTime);
        String responseBody = requestGet(weatherApiUrls.MID_TEMPERATURE_FORECAST, parameters);

        TempForecastResponseItem tempForecastResponseItem = WeatherApiUtil.parseTempForecast(responseBody);
        return tempForecastResponseItem.toTemperatureList(announceTime);
    }

    public List<Pop> requestMidTermLandForecast(String regionId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime announceTime = WeatherApiUtil.getMidTermLatestAnnounceTime(now);

        Map<String, String> parameters = getMidTermParameters(regionId, announceTime);
        String responseBody = requestGet(weatherApiUrls.MID_LAND_FORECAST, parameters);

        LandForecastResponseItem landForecastResponseItem = WeatherApiUtil.parseLandForecast(responseBody);
        return landForecastResponseItem.toPopList(announceTime);
    }


    private GridForecast requestShortTermGridForecastWithTimes(String forecastVar, LocalDateTime announceTime, LocalDateTime targetTime) {
        Map<String, String> parameters = getShortGridParameters(forecastVar, announceTime, targetTime);

        String responseBody = requestGet(weatherApiUrls.SHORT_GRID_FORECAST, parameters);

        return new GridForecast(
                announceTime,
                targetTime,
                forecastVar,
                WeatherApiUtil.parseGridData(responseBody)
        );
    }

    private String requestGet(String url, Map<String, String> variables) {
        return restClient.get().uri(url, variables).retrieve().body(String.class);
    }

    private Map<String, String> getShortGridParameters(String forecastVar, LocalDateTime announceTime, LocalDateTime targetTime) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("tmfc", WeatherApiUtil.formatToShortTermTime(announceTime));
        parameters.put("tmef",  WeatherApiUtil.formatToShortTermTime(targetTime));
        parameters.put("vars", forecastVar);
        parameters.put("authKey", apiKey);
        return parameters;
    }

    private Map<String, String> getMidTermParameters(String regionId, LocalDateTime announceTime) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("pageNo", "1");
        parameters.put("numOfRows", "1");
        parameters.put("dataType", "JSON");
        parameters.put("regId", regionId);
        parameters.put("tmFc", WeatherApiUtil.formatToMidTermTime(announceTime));
        parameters.put("authKey", apiKey);

        return parameters;
    }

    private Map<String, String> getShortLandParameters(String regionId) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("pageNo", "1");
        parameters.put("numOfRows", "9");
        parameters.put("dataType", "JSON");
        parameters.put("regId", regionId);
        parameters.put("authKey", apiKey);

        return parameters;
    }
}