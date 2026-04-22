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
        List<ShortGrid> shortGrids = fetchRecentShortGrids(cityRegionCode);
        LocalDateTime announceTime = extractAnnounceTime(shortGrids);
        List<ForecastHourlyPoint> forecastHourlyPoints = buildHourlyPoints(shortGrids);

        return new HourlyComposeResult(announceTime, forecastHourlyPoints);
    }

    private List<ShortGrid> fetchRecentShortGrids(CityRegionCode cityRegionCode) {
        Coordinates coords = cityRegionCode.getCoordinates();
        return shortGridRepository.findRecentByXAndY(coords.getX(), coords.getY());
    }

    private LocalDateTime extractAnnounceTime(List<ShortGrid> shortGrids) {
        return shortGrids.isEmpty()
                ? null
                : shortGrids.get(0).getAnnounceTime().getTime();
    }

    private List<ForecastHourlyPoint> buildHourlyPoints(List<ShortGrid> shortGrids) {
        return shortGrids.stream()
                .filter(sg -> sg.getEffectiveTime() != null)
                .sorted(Comparator.comparing(ShortGrid::getEffectiveTime))
                .limit(MAX_HOURLY_POINTS)
                .map(sg -> new ForecastHourlyPoint(
                        sg.getEffectiveTime(), sg.getTemp(), sg.getPop()))
                .toList();
    }
}