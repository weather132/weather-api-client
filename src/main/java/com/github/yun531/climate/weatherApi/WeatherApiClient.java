package com.github.yun531.climate.weatherApi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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

    public List<List<Float>> RequestShortTermGridForecast(String targetTime, ForecastCategory forecastVar) throws URISyntaxException {
        String responseBody = restClient.get()
                .uri(new URI(weatherApiUrls.SHORT_GRID_FORECAST))
                .attribute("tmfc", getLatestAnnounceTime())
                .attribute("tmef", targetTime)
                .attribute("vars", forecastVar)
                .attribute("authKey", apiKey)
                .retrieve()
                .body(String.class);

        return parseGridData(responseBody);
    }

    private String getLatestAnnounceTime() {
        LocalDateTime nowDateTime = LocalDateTime.now();
        int nowHour = nowDateTime.getHour();
        final int[] announceTime = {2, 5, 8, 11, 14, 17, 20, 23};
        int latestAnnounceHour = Arrays.stream(announceTime).filter((h) -> h <= nowHour)
                .max()
                .orElse(23);

        String latestAnnounceHourStr = makeHourTo2digitHour(latestAnnounceHour);

        LocalDate nowLocalDate = nowDateTime.toLocalDate();
        return nowLocalDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + latestAnnounceHourStr;
    }

    private String makeHourTo2digitHour(int latestAnnounceHour) {
        final List<Integer> oneDigitHours = Arrays.asList(2, 5, 8);
        String latestAnnounceHourStr = Integer.toString(latestAnnounceHour);
        latestAnnounceHourStr = oneDigitHours.contains(latestAnnounceHour) ? "0" + latestAnnounceHourStr : latestAnnounceHourStr;
        return latestAnnounceHourStr;
    }

    private List<List<Float>> parseGridData(String responseBody) {
        final int ROW_SIZE = 149;
        final int COL_SIZE = 253;

        List<Float> bodyList = Arrays.stream(responseBody.split(","))
                .map(Float::parseFloat)
                .toList();

        List<List<Float>> gridData = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE * COL_SIZE; i += ROW_SIZE) {
            gridData.add(bodyList.subList(i, Math.min(i + ROW_SIZE, bodyList.size())));
        }
        return gridData;
    }
}
