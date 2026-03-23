package com.github.yun531.climate.snapshot.application;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.Coordinates;
import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import com.github.yun531.climate.shortGrid.domain.ShortGridRepository;
import com.github.yun531.climate.snapshot.domain.model.HourlyForecastItem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class HourlyForecastComposer {

    private final ShortGridRepository shortGridRepository;

    public HourlyForecastComposer(ShortGridRepository shortGridRepository) {
        this.shortGridRepository = shortGridRepository;
    }

    public List<HourlyForecastItem> compose(
            CityRegionCode regionCode, LocalDateTime announceTime
    ) {
        Coordinates coords = regionCode.getCoordinates();
        AnnounceTime at = new AnnounceTime(announceTime);

        List<ShortGrid> shortGrids = shortGridRepository
                .findByAnnounceTimeAndXAndY(at, coords.getX(), coords.getY());

        return shortGrids.stream()
                .map(HourlyForecastItem::from)
                .toList();
    }
}