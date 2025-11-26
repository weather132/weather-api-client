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

@RedisHash(value = "weather", timeToLive = 3600 * 24)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Weather {

    // weather:발표시간:좌표:발효시간
    @Id
    private String id;

    private Integer pop;
    private Integer maxTemp;
    private Integer minTemp;

    public Weather(String announceTime, String coords, String effectTime, Integer pop, Integer maxTemp, Integer minTemp) {
        this.id = announceTime + ":" + coords + ":" + effectTime;
        this.pop = pop;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
    }

    public Weather(LocalDateTime announceTime, String coords, LocalDateTime effectTime, Integer pop, Integer maxTemp, Integer minTemp) {
        this.id = WeatherApiUtil.formatToMidTermTime(announceTime) + ":"
                + coords + ":"
                + WeatherApiUtil.formatToMidTermTime(effectTime);

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
