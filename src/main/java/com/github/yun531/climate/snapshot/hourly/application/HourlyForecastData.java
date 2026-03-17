package com.github.yun531.climate.snapshot.hourly.application;

import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class HourlyForecastData {
    private String effectiveTime;
    private Integer pop;
    private Integer temp;

    public HourlyForecastData(ShortGrid shortGrid) {
        this.effectiveTime = shortGrid.getEffectiveTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.pop = shortGrid.getPop();
        this.temp = shortGrid.getTemp();
    }
}