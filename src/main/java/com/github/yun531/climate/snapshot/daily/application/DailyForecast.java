package com.github.yun531.climate.snapshot.daily.application;

import com.github.yun531.climate.snapshot.daily.domain.DailyForecastItem;
import lombok.Data;

import java.util.List;

@Data
public class DailyForecast {
    String regionCode;
    List<DailyForecastItem> forecasts;

    public DailyForecast(String regionCode, List<DailyForecastItem> forecasts) {
        this.regionCode = regionCode;
        this.forecasts = forecasts;
    }
}
