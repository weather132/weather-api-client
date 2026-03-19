package com.github.yun531.climate.snapshot.daily.domain;

import com.github.yun531.climate.shortLand.domain.ShortLand;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class DailyForecastItem {
    private final String announceTime;
    private final String effectiveTime;
    private final Integer temp;
    private final Integer pop;

    public DailyForecastItem(ShortLand shortLand) {
        this.announceTime = shortLand.getAnnounceTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.effectiveTime = shortLand.getEffectiveTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.temp = shortLand.getTemp();
        this.pop = shortLand.getPop();
    }

    public DailyForecastItem(String announceTime, String effectiveTime, Integer temp, Integer pop) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.temp = temp;
        this.pop = pop;
    }
}
