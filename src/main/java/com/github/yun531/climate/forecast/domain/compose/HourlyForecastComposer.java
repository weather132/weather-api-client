package com.github.yun531.climate.forecast.domain.compose;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.Coordinates;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyPoint;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import com.github.yun531.climate.shortGrid.domain.ShortGridRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * ShortGrid DB 조회 → ForecastHourlyPoint 직접 생산.
 */
@Component
public class HourlyForecastComposer {

    private static final int MAX_HOURLY_POINTS = 26;

    private final ShortGridRepository shortGridRepository;

    public HourlyForecastComposer(ShortGridRepository shortGridRepository) {
        this.shortGridRepository = shortGridRepository;
    }

    public record HourlyComposeResult(
            LocalDateTime announceTime,
            List<ForecastHourlyPoint> forecastHourlyPoints
    ) {}

    public HourlyComposeResult compose(CityRegionCode cityRegionCode) {
        Coordinates coords = cityRegionCode.getCoordinates();

        List<ShortGrid> shortGrids = shortGridRepository
                .findRecentByXAndY(coords.getX(), coords.getY());

        LocalDateTime announceTime = shortGrids.isEmpty()
                ? null
                : shortGrids.get(0).getAnnounceTime().getTime();

        List<ForecastHourlyPoint> forecastHourlyPoints = shortGrids.stream()
                .filter(sg -> sg.getEffectiveTime() != null)
                .sorted(Comparator.comparing(ShortGrid::getEffectiveTime))
                .limit(MAX_HOURLY_POINTS)
                .map(sg -> new ForecastHourlyPoint(
                        sg.getEffectiveTime(), sg.getTemp(), sg.getPop()))
                .toList();

        return new HourlyComposeResult(announceTime, forecastHourlyPoints);
    }
}