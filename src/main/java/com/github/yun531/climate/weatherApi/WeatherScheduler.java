package com.github.yun531.climate.weatherApi;

import com.github.yun531.climate.dto.*;
import com.github.yun531.climate.entity.*;
import com.github.yun531.climate.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Component
public class WeatherScheduler {
    private final WeatherApiClient weatherApiClient;
    private final ShortGridBatchRepository shortGridBatchRepository;
    private final ShortLandBatchRepository shortLandBatchRepository;
    private final MidPopBatchRepository midPopBatchRepository;
    private final MidTemperatureRepository midTemperatureRepository;
    private final ProvinceRegionCodeRepository provinceRegionCodeRepository;
    private final CityRegionCodeRepository cityRegionCodeRepository;

    @Autowired
    public WeatherScheduler(WeatherApiClient weatherApiClient, ShortGridBatchRepository shortGridBatchRepository, ShortLandBatchRepository shortLandBatchRepository, MidPopBatchRepository midPopBatchRepository, MidTemperatureRepository midTemperatureRepository, ProvinceRegionCodeRepository provinceRegionCodeRepository, CityRegionCodeRepository cityRegionCodeRepository) {
        this.weatherApiClient = weatherApiClient;
        this.shortGridBatchRepository = shortGridBatchRepository;
        this.shortLandBatchRepository = shortLandBatchRepository;
        this.midPopBatchRepository = midPopBatchRepository;
        this.midTemperatureRepository = midTemperatureRepository;
        this.provinceRegionCodeRepository = provinceRegionCodeRepository;
        this.cityRegionCodeRepository = cityRegionCodeRepository;
    }

    @Scheduled(cron = "0 10 2/3 * * *")
    @Transactional
    public void updateShortTermGrid() {
        updateShortForecasts();
    }

    @Scheduled(cron = "0 10 6,18 * * *")
    @Transactional
    public void updateMidTerm() {
        updateMidTemperature();
        updateMidPop();
    }

    @Scheduled(cron = "0 10 5,11,17 * * *")
    @Transactional
    public void updateShortTermLand() {

        List<ShortLand> shortLands = cityRegionCodeRepository.findAll().stream()
                .map(CityRegionCode::getRegionCode)
                .map(weatherApiClient::requestShortTermLandForecast)
                .flatMap(Collection::stream)
                .map(shortLand -> shortLand.toEntity(cityRegionCodeRepository.findByRegionCode(shortLand.getRegionId())))
                .toList();

        shortLandBatchRepository.saveAll(shortLands);
    }


    private void updateShortForecasts() {
        List<Coordinates> coordinates = getCoords();

        for (int i = 1; i < 27; i++) {
            GridForecast popGrid = weatherApiClient.requestShortTermGridForecast(i, ForecastCategory.POP);
            GridForecast tempGrid = weatherApiClient.requestShortTermGridForecast(i, ForecastCategory.TEMP);

            LocalDateTime announceTime = popGrid.getAnnounceTime();
            LocalDateTime effectiveTime = popGrid.getEffectiveTime();

            List<ShortGrid> shortGrids = coordinates.stream()
                    .map(coords -> new ShortGrid(
                            null,
                            announceTime,
                            effectiveTime,
                            coords.x(),
                            coords.y(),
                            popGrid.getForecastValue(coords.x(), coords.y()),
                            tempGrid.getForecastValue(coords.x(), coords.y())))
                    .toList();

            shortGridBatchRepository.saveAll(shortGrids);
        }
    }

    private List<Coordinates> getCoords() {
        return cityRegionCodeRepository.findAll().stream()
                .map(regionCode -> new Coordinates(regionCode.getX(), regionCode.getY()))
                .distinct()
                .toList();
    }


    private void updateMidTemperature() {
        midTemperatureRepository.saveAll(getMidTemps());
    }

    private void updateMidPop() {
        midPopBatchRepository.saveAll(getMidPops());
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
