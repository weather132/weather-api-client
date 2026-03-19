package com.github.yun531.climate.snapshot.hourly.application;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.cityRegionCode.domain.Coordinates;
import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import com.github.yun531.climate.shortGrid.domain.ShortGridRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HourlyService {
    private final CityRegionCodeRepository regionCodeRepository;
    private final ShortGridRepository shortGridRepository;

    public HourlyService(CityRegionCodeRepository regionCodeRepository, ShortGridRepository shortGridRepository) {
        this.regionCodeRepository = regionCodeRepository;
        this.shortGridRepository = shortGridRepository;
    }


    public HourlyForecastDto getSnapshot(AnnounceTime announceTime, String regionCode) {
        Coordinates coords = regionCodeRepository.findByRegionCode(regionCode).getCoordinates();

        List<ShortGrid> shortGrids = shortGridRepository.findByAnnounceTimeAndXAndY(announceTime, coords.getX(), coords.getY());

        List<HourlyForecastData> data = toHourlyForecastData(shortGrids);

        return new HourlyForecastDto(announceTime.formatIso(), coords.getX(), coords.getY(), data);
    }


    private List<HourlyForecastData> toHourlyForecastData(List<ShortGrid> shortGrids) {
        return shortGrids.stream()
                .map(HourlyForecastData::new)
                .toList();
    }
}
