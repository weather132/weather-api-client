package com.github.yun531.climate.snapshot.domain.model;

import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class HourlyForecastItem {

    private final LocalDateTime effectiveTime;
    private final Integer pop;
    private final Integer temp;

    public HourlyForecastItem(
            LocalDateTime effectiveTime, Integer pop, Integer temp
    ) {
        this.effectiveTime = effectiveTime;
        this.pop = pop;
        this.temp = temp;
    }

    public static HourlyForecastItem from(ShortGrid shortGrid) {
        return new HourlyForecastItem(
                shortGrid.getEffectiveTime(),
                shortGrid.getPop(),
                shortGrid.getTemp()
        );
    }
}