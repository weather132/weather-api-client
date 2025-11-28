package com.github.yun531.climate.weatherApi;

import com.github.yun531.climate.dto.CoordsForecast;
import com.github.yun531.climate.dto.GridForecast;
import com.github.yun531.climate.entity.DayWeather;
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

    @Scheduled(cron = "0 10 2/3 * * *")
    public void doShortTermGridEarly() {
        List<DayWeather> dayWeathers = IntStream.range(1, 25)
                .mapToObj(this::requestAndGetWeathers)
                .flatMap(List::stream)
                .toList();

        weatherRepository.saveAll(dayWeathers);
    }


    private List<DayWeather> requestAndGetWeathers(int hours) {
        GridForecast maxTempGrid = weatherApiClient.requestShortTermGridForecast(hours, ForecastCategory.MAX_TEMP);
        GridForecast minTempGrid = weatherApiClient.requestShortTermGridForecast(hours, ForecastCategory.MIN_TEMP);
        GridForecast popGrid = weatherApiClient.requestShortTermGridForecast(hours, ForecastCategory.POP);

        List<DayWeather> weatherList = new ArrayList<>();
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
            String coords = popCoords.getX() + ":"  + popCoords.getY();
            LocalDateTime effectiveTime = popGrid.getEffectiveTime();

            weatherList.add(new DayWeather(announceTime, coords, effectiveTime, popCoords.getValue(),  maxTemp.getValue(), minTemp.getValue() ));
        }

        return weatherList;

    }
}
