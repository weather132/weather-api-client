package com.github.yun531.climate.entity;

import com.github.yun531.climate.util.WeatherApiUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

// temperature:발표시간:발효시간:x좌표:y좌표
@Data
@RedisHash(value = "temperature")
@NoArgsConstructor
public class TemperatureEntity {

    @Id
    private String id;

    private Integer MaxTemp;
    private Integer MinTemp;

    public TemperatureEntity(LocalDateTime announceTime, LocalDateTime effectiveTime, int x, int y, int maxTemp, int minTemp) {
        id = WeatherApiUtil.formatToMidTermTime(announceTime)
                + ":" + WeatherApiUtil.formatToMidTermTime(effectiveTime)
                + ":" + x
                + ":" + y;
        MaxTemp = maxTemp;
        MinTemp = minTemp;
    }
}
