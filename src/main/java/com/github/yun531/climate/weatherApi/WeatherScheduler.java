package com.github.yun531.climate.weatherApi;

import com.github.yun531.climate.dto.CoordsForecast;
import com.github.yun531.climate.dto.GridForecast;
import com.github.yun531.climate.entity.ShortPop;
import com.github.yun531.climate.entity.ShortTemperature;
import com.github.yun531.climate.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class WeatherScheduler {
    private final WeatherApiClient weatherApiClient;
    private final ShortPopRepository shortPopRepository;
    private final ShortTemperatureRepository shortTempRepository;
    private final MidPopRepository midPopRepository;
    private final MidTemperatureRepository midTemperatureRepository;

    @Autowired
    public WeatherScheduler(WeatherApiClient weatherApiClient, ShortPopRepository shortPopRepository, ShortTemperatureRepository ShortTempRepository, MidPopRepository midPopRepository, MidTemperatureRepository midTemperatureRepository) {
        this.weatherApiClient = weatherApiClient;
        this.shortPopRepository = shortPopRepository;
        this.shortTempRepository = ShortTempRepository;
        this.midPopRepository = midPopRepository;
        this.midTemperatureRepository = midTemperatureRepository;
    }

    @Scheduled(cron = "0 10 2/3 * * *")
    public void doShortTermGrid() {
        updateShortTermTemperature();
        updateShortTermPop();
    }

    private void updateShortTermPop() {
        List<ShortPop> pops = IntStream.range(1, 25)
                .mapToObj(h -> weatherApiClient.requestShortTermGridForecast(h, ForecastCategory.POP))
                .map(ShortPop::of)
                .flatMap(Collection::stream)
                .toList();

        shortPopRepository.saveAll(pops);
    }

    private void updateShortTermTemperature() {
        GridForecast maxTempGrid = weatherApiClient.requestShortTermGridForecastAfterDays(1, ForecastCategory.MAX_TEMP);
        GridForecast minTempGrid = weatherApiClient.requestShortTermGridForecastAfterDays(1, ForecastCategory.MIN_TEMP);
        LocalDateTime tempAnnounceTime = maxTempGrid.getAnnounceTime();
        LocalDateTime tempEffectiveTime = maxTempGrid.getEffectiveTime();
        List<CoordsForecast> maxTempCoords = maxTempGrid.getCoordsForecastList();
        List<CoordsForecast> minTempCoords = minTempGrid.getCoordsForecastList();

        List<ShortTemperature> temps = new ArrayList<>();
        for (CoordsForecast maxTemp : maxTempCoords) {
            int x = maxTemp.getX();
            int y = maxTemp.getY();
            CoordsForecast minTemp = minTempCoords.stream()
                    .filter(coords -> coords.getX() == x && coords.getY() == y)
                    .findFirst()
                    .get();

            temps.add(new ShortTemperature(tempAnnounceTime, tempEffectiveTime, x, y, maxTemp.getValue(), minTemp.getValue()));
        }

        shortTempRepository.saveAll(temps);
    }
}
