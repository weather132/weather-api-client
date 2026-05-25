package com.github.yun531.climate.forecast.domain.compose;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.Coordinates;
import com.github.yun531.climate.forecast.domain.readmodel.FcstHourlyPoint;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import com.github.yun531.climate.shortGrid.domain.ShortGridRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * ShortGrid DB 조회 → ForecastHourlyPoint 직접 생산.
 */
@Slf4j
@Component
public class HourlyFcstComposer {

    private static final int MAX_HOURLY_POINTS = 26;

    private final ShortGridRepository shortGridRepository;

    public HourlyFcstComposer(ShortGridRepository shortGridRepository) {
        this.shortGridRepository = shortGridRepository;
    }

    public record HourlyComposeResult(
            LocalDateTime announceTime,
            List<FcstHourlyPoint> fcstHourlyPoints
    ) {}

    public HourlyComposeResult compose(CityRegionCode cityRegionCode) {
        List<ShortGrid> shortGrids = fetchRecentShortGrids(cityRegionCode);
        LocalDateTime announceTime = extractAnnounceTime(shortGrids);
        List<FcstHourlyPoint> fcstHourlyPoints = buildHourlyPoints(shortGrids);

        HourlyComposeResult result = new HourlyComposeResult(announceTime, fcstHourlyPoints);
        logIfDegraded(cityRegionCode, result);
        return result;
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

    private List<FcstHourlyPoint> buildHourlyPoints(List<ShortGrid> shortGrids) {
        return shortGrids.stream()
                .filter(sg -> sg.getEffectiveTime() != null)
                .sorted(Comparator.comparing(ShortGrid::getEffectiveTime))
                .limit(MAX_HOURLY_POINTS)
                .map(sg -> new FcstHourlyPoint(
                        sg.getEffectiveTime(), sg.getTemp(), sg.getPop()))
                .toList();
    }

    private void logIfDegraded(CityRegionCode regionCode, HourlyComposeResult result) {
        String regionId = regionCode.getRegionCode();

        if (result.fcstHourlyPoints().isEmpty()) {
            log.warn("HourlyForecast 결과 없음. regionId={}", regionId);
            return;
        }

        long emptySlots = result.fcstHourlyPoints().stream()
                .filter(p -> p.temp() == null || p.pop() == null)
                .count();
        int missingPoints = Math.max(0, MAX_HOURLY_POINTS - result.fcstHourlyPoints().size());

        if (emptySlots > 0 || missingPoints > 0) {
            log.warn("HourlyForecast 결손 슬롯. regionId={} emptySlots={} missingPoints={}",
                    regionId, emptySlots, missingPoints);
        }
    }
}