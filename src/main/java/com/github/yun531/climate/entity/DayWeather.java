package com.github.yun531.climate.entity;

import com.github.yun531.climate.util.WeatherApiUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RedisHash(value = "dayweather", timeToLive = 3600 * 2)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DayWeather {

    // dayweather:발표시간:좌표:발효시간
    @Id
    private String id;

    private Integer pop;
    private Integer maxTemp;
    private Integer minTemp;


    public DayWeather(LocalDateTime announceTime, LocalDateTime effectTime, int x, int y, Integer pop, Integer maxTemp, Integer minTemp) {
        this.id = WeatherApiUtil.formatToMidTermTime(announceTime)
                + ":" + WeatherApiUtil.formatToMidTermTime(effectTime)
                + ":" + x
                + ":" + y;

        this.pop = pop;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
    }

    public String getAnnounceTime() {
        return parseId().get(0);
    }

    public String getCoords() {
        return parseId().get(1);
    }

    public String getEffectTime() {
        return parseId().get(2);
    }

    private List<String> parseId() {
        return Arrays.stream(this.id.split(":")).toList();
    }
}
