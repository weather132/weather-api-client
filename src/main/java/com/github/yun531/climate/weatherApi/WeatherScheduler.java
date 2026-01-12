package com.github.yun531.climate.weatherApi;

import com.github.yun531.climate.dto.*;
import com.github.yun531.climate.entity.*;
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
    private final ShortLandForecastRepository shortLandForecastRepository;
    private final MidPopRepository midPopRepository;
    private final MidTemperatureRepository midTemperatureRepository;
    private final ProvinceRegionCodeRepository provinceRegionCodeRepository;
    private final CityRegionCodeRepository cityRegionCodeRepository;

    @Autowired
    public WeatherScheduler(WeatherApiClient weatherApiClient, ShortPopRepository shortPopRepository, ShortTemperatureRepository ShortTempRepository, ShortLandForecastRepository landForecastRepository, MidPopRepository midPopRepository, MidTemperatureRepository midTemperatureRepository, ProvinceRegionCodeRepository provinceRegionCodeRepository, CityRegionCodeRepository cityRegionCodeRepository) {
        this.weatherApiClient = weatherApiClient;
        this.shortPopRepository = shortPopRepository;
        this.shortTempRepository = ShortTempRepository;
        this.shortLandForecastRepository = landForecastRepository;
        this.midPopRepository = midPopRepository;
        this.midTemperatureRepository = midTemperatureRepository;
        this.provinceRegionCodeRepository = provinceRegionCodeRepository;
        this.cityRegionCodeRepository = cityRegionCodeRepository;
    }

    @Scheduled(cron = "0 10 2/3 * * *")
    public void updateShortTermGrid() {
        updateShortTemperature();
        updateShortPop();
    }

    @Scheduled(cron = "0 10 6,18 * * *")
    public void updateMidTerm() {
        updateMidTemperature();
        updateMidPop();
    }

    @Scheduled(cron = "0 10 5,11,17 * * *")
    public void updateShortTermLand() {

        List<ShortLand> shortLands = cityRegionCodeRepository.findAll().stream()
                .map(CityRegionCode::getRegionCode)
                .map(weatherApiClient::requestShortTermLandForecast)
                .flatMap(Collection::stream)
                .map(shortLand -> shortLand.toEntity(cityRegionCodeRepository.findByRegionCode(shortLand.getRegionId())))
                .toList();

        shortLandForecastRepository.saveAll(shortLands);
    }


    private void updateShortPop() {
        List<ShortPop> pops = IntStream.range(1, 26)
                .mapToObj(h -> weatherApiClient.requestShortTermGridForecast(h, ForecastCategory.POP))
                .map(ShortPop::of)
                .flatMap(Collection::stream)
                .toList();

        shortPopRepository.saveAll(pops);
    }

    private void updateShortTemperature() {
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

    private void updateMidTemperature() {
        midTemperatureRepository.saveAll(getMidTemps());
    }

    private void updateMidPop() {
        midPopRepository.saveAll(getMidPops());
    }

    private List<MidTemperature> getMidTemps() {
        return cityRegionCodeRepository.findAll().stream()
                .map(code -> weatherApiClient.requestMidTermTempForecast(code.getRegionCode()).stream()
                        .map(temp -> temp.toMidTemperatureEntity(code))
                        .toList())
                .flatMap(Collection::stream)
                .toList();
    }

    private List<MidPop> getMidPops() {
        return provinceRegionCodeRepository.findAll().stream()
                .map(code -> weatherApiClient.requestMidTermLandForecast(code.getRegionCode()).stream()
                        .map(pop -> pop.toMidPopEntity(code))
                        .toList())
                .flatMap(Collection::stream)
                .toList();
    }
}
