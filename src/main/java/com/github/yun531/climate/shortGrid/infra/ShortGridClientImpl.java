package com.github.yun531.climate.shortGrid.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.cityRegionCode.domain.Coordinates;
import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.common.client.WeatherClient;
import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import com.github.yun531.climate.shortGrid.domain.ShortGridClient;
import com.github.yun531.climate.shortGrid.infra.config.ForecastVariable;
import com.github.yun531.climate.shortGrid.infra.config.ShortGridUrl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
@Qualifier("short-grid-client")
public class ShortGridClientImpl implements ShortGridClient {
    private final WeatherClient client;
    private final ShortGridUrl url;
    private final ApiKey apiKey;
    private final ForecastVariable fcstVar;
    private final CityRegionCodeRepository cityRegionCodeRepository;

    public ShortGridClientImpl(WeatherClient client, ShortGridUrl url, ApiKey apiKey, ForecastVariable fcstVar, CityRegionCodeRepository cityRegionCodeRepository) {
        this.client = client;
        this.url = url;
        this.apiKey = apiKey;
        this.fcstVar = fcstVar;
        this.cityRegionCodeRepository = cityRegionCodeRepository;
    }

    @Override
    public List<ShortGrid> requestShortGrids(AnnounceTime announceTime, LocalDateTime effectiveTime) {
        GridData popGridData = new GridData(request(announceTime, effectiveTime, fcstVar.getPop()));
        GridData tempGridData = new GridData(request(announceTime, effectiveTime, fcstVar.getTemperature()));

        return gridDataToShortGrid(announceTime, effectiveTime, popGridData, tempGridData);
    }

    @Override
    public List<ShortGrid> requestShortGridsForHours(AnnounceTime announceTime, int hours) {
        return IntStream.range(1, hours)
                .mapToObj(hour -> announceTime.getAnnounceTime().plusHours(hour))
                .map(effectiveTime -> requestShortGrids(announceTime, effectiveTime))
                .flatMap(List::stream)
                .toList();
    }


    private Map<String, String> makeParams(AnnounceTime announceTime, LocalDateTime effectiveTime, String fcstVar) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("tmfc", formatTime(announceTime.getAnnounceTime()));
        parameters.put("tmef",  formatTime(effectiveTime));
        parameters.put("vars", fcstVar);
        parameters.put("authKey", apiKey.getApiKey());
        return parameters;
    }

    private String formatTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
    }

    private String request(AnnounceTime announceTime, LocalDateTime effectiveTime, String fcstVar) {
        return client.requestGet(url.getUrl(), makeParams(announceTime, effectiveTime, fcstVar));
    }

    private List<ShortGrid> gridDataToShortGrid(AnnounceTime announceTime, LocalDateTime effectiveTime, GridData popGridData, GridData tempGridData) {
        List<Coordinates> coords = getCoords();

        return coords.stream()
                .map(coordinates -> new ShortGrid(
                        announceTime,
                        effectiveTime,
                        coordinates.getX(),
                        coordinates.getY(),
                        popGridData.getData(coordinates.getX(), coordinates.getY()),
                        tempGridData.getData(coordinates.getX(), coordinates.getY())))
                .toList();
    }

    private List<Coordinates> getCoords() {
        return cityRegionCodeRepository.findAll().stream().map(CityRegionCode::getCoordinates).toList();
    }
}
