package com.github.yun531.climate.weatherApi;

import com.github.yun531.climate.dto.CoordsForecast;
import com.github.yun531.climate.dto.GridForecast;
import com.github.yun531.climate.entity.PopEntity;
import com.github.yun531.climate.entity.TemperatureEntity;
import com.github.yun531.climate.repository.PopRepository;
import com.github.yun531.climate.repository.TemperatureRepository;
import com.github.yun531.climate.repository.WeatherRepository;
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
    private final WeatherRepository weatherRepository;
    private final PopRepository popRepository;
    private final TemperatureRepository temperatureRepository;

    @Autowired
    public WeatherScheduler(WeatherApiClient weatherApiClient, WeatherRepository weatherRepository, PopRepository popRepository, TemperatureRepository temperatureRepository) {
        this.weatherApiClient = weatherApiClient;
        this.weatherRepository = weatherRepository;
        this.popRepository = popRepository;
        this.temperatureRepository = temperatureRepository;
    }

    @Scheduled(cron = "0 10 2/3 * * *")
    public void doShortTermGrid() {
        updateShortTermTemperature();
        updateShortTermPop();
    }

    private void updateShortTermPop() {
        List<PopEntity> pops = IntStream.range(1, 25)
                .mapToObj(h -> weatherApiClient.requestShortTermGridForecast(h, ForecastCategory.POP))
                .map(PopEntity::of)
                .flatMap(Collection::stream)
                .toList();

        pops.forEach(popRepository::save);
    }

    private void updateShortTermTemperature() {
        GridForecast maxTempGrid = weatherApiClient.requestShortTermGridForecastAfterDays(1, ForecastCategory.MAX_TEMP);
        GridForecast minTempGrid = weatherApiClient.requestShortTermGridForecastAfterDays(1, ForecastCategory.MIN_TEMP);
        LocalDateTime tempAnnounceTime = maxTempGrid.getAnnounceTime();
        LocalDateTime tempEffectiveTime = maxTempGrid.getEffectiveTime();
        List<CoordsForecast> maxTempCoords = maxTempGrid.getCoordsForecastList();
        List<CoordsForecast> minTempCoords = minTempGrid.getCoordsForecastList();
        List<TemperatureEntity> temps = new ArrayList<>();
        for (CoordsForecast maxTemp : maxTempCoords) {
            int x = maxTemp.getX();
            int y = maxTemp.getY();
            CoordsForecast minTemp = minTempCoords.stream()
                    .filter(coords -> coords.getX() == x && coords.getY() == y)
                    .findFirst()
                    .get();

            temps.add(new TemperatureEntity(tempAnnounceTime, tempEffectiveTime, x, y, maxTemp.getValue(), minTemp.getValue()));
        }

        temperatureRepository.saveAll(temps);
    }
}
