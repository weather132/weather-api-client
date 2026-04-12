package com.github.yun531.climate.snapshot.domain.model;

import com.github.yun531.climate.shortLand.domain.ShortLand;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DailyForecastItem {

    private final LocalDateTime announceTime;
    private final LocalDateTime effectiveTime;
    private final Integer temp;
    private final Integer pop;

    public DailyForecastItem(
            LocalDateTime announceTime,
            LocalDateTime effectiveTime,
            Integer temp,
            Integer pop
    ) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.temp = temp;
        this.pop = pop;
    }

    public static DailyForecastItem from(ShortLand shortLand) {
        return new DailyForecastItem(
                shortLand.getAnnounceTime(),
                shortLand.getEffectiveTime(),
                shortLand.getTemp(),   //todo 일단 여기서 터지는듯
                shortLand.getPop()
        );
    }
}