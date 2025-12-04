package com.github.yun531.climate.entity;

import com.github.yun531.climate.dto.GridForecast;
import com.github.yun531.climate.util.WeatherApiUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PopEntity {
    private LocalDateTime announceTime;
    private LocalDateTime effectiveTime;
    private Integer x;
    private Integer y;
    private Integer pop;

    public String getKey() {
        return "pop"
                + ":" + WeatherApiUtil.formatToMidTermTime(announceTime)
                + ":" + WeatherApiUtil.formatToMidTermTime(effectiveTime)
                + ":" + x
                + ":" + y;
    }

    public static List<PopEntity> of(GridForecast gridForecast) {
        LocalDateTime announceTime = gridForecast.getAnnounceTime();
        LocalDateTime effectiveTime = gridForecast.getEffectiveTime();

        return gridForecast.getCoordsForecastList().stream()
                .map(coords -> new PopEntity(announceTime, effectiveTime, coords.getX(), coords.getY(), coords.getValue()))
                .toList();
    }
}
