package com.github.yun531.climate.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Arrays;
import java.util.List;

// 7800초 = 2시간 10분
@RedisHash(value = "weather", timeToLive = 7800)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Weather {

    // weather:발표시간:좌표:밣효시간
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
