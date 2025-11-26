package com.github.yun531.climate.weatherApi;

import com.github.yun531.climate.dto.CoordsForecast;
import com.github.yun531.climate.dto.GridForecast;
import com.github.yun531.climate.entity.Weather;
import com.github.yun531.climate.repository.WeatherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class WeatherScheduler {
    private final WeatherApiClient weatherApiClient;
    private final WeatherRepository weatherRepository;

    @Autowired
    public WeatherScheduler(WeatherApiClient weatherApiClient, WeatherRepository weatherRepository) {
        this.weatherApiClient = weatherApiClient;
        this.weatherRepository = weatherRepository;
    }

    @Scheduled(cron = "0 10 2,5,8,11,14 * * *")
    public void doShortTermGridEarly() {
        int hour = LocalDateTime.now().getHour();

        List<Weather> weathers = IntStream.range(1, 24 * 3 - hour + 1)
                .mapToObj(this::requestAndGetWeathers)
                .flatMap(List::stream)
                .toList();

        weatherRepository.saveAll(weathers);
    }

    @Scheduled(cron = "0 10 17,20,23 * * *")
    public void doShortTermGridLate() {
        int hour = LocalDateTime.now().getHour();

        List<Weather> weathers = IntStream.range(1, 24 * 4 - hour + 1)
                .mapToObj(this::requestAndGetWeathers)
                .flatMap(List::stream)
                .toList();

        weatherRepository.saveAll(weathers);
    }


    private List<Weather> requestAndGetWeathers(int hours) {
        GridForecast maxTempGrid = weatherApiClient.requestShortTermGridForecast(hours, ForecastCategory.MAX_TEMP);
        GridForecast minTempGrid = weatherApiClient.requestShortTermGridForecast(hours, ForecastCategory.MIN_TEMP);
        GridForecast popGrid = weatherApiClient.requestShortTermGridForecast(hours, ForecastCategory.POP);

        List<Weather> weatherList = new ArrayList<>();
        List<CoordsForecast> popCoordsForecasts = popGrid.getCoordsForecastList();
        for (CoordsForecast popCoords :  popCoordsForecasts) {
            CoordsForecast maxTemp = maxTempGrid.getCoordsForecastList().stream()
                    .filter(maxTempCoords -> maxTempCoords.isSameCoords(popCoords))
                    .findAny()
                    .get();

            CoordsForecast minTemp = minTempGrid.getCoordsForecastList().stream()
                    .filter(minTempCoords -> minTempCoords.isSameCoords(popCoords))
                    .findAny()
                    .get();

            LocalDateTime announceTime = popGrid.getAnnounceTime();
            String coords = popCoords.getX() + "-"  + popCoords.getY();
            LocalDateTime effectiveTime = popGrid.getEffectiveTime();

            weatherList.add(new Weather(announceTime, coords, effectiveTime, popCoords.getValue(),  maxTemp.getValue(), minTemp.getValue() ));
        }

        return weatherList;

    }
}
