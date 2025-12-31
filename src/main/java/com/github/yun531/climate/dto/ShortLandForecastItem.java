package com.github.yun531.climate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.yun531.climate.entity.ShortLandForecast;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ShortLandForecastItem {
    private String announceTime;

    @JsonProperty("regId")
    private String regionId;

    @JsonProperty("numEf")
    private Integer numEf;

    @JsonProperty("rnSt")
    private Integer pop;

    @JsonProperty("temp")
    private Integer ta;

    @JsonProperty("rnYn")
    private Integer rainType;

    public ShortLandForecast toEntity() {
        LocalDateTime parsedAnnounceTime = LocalDateTime.parse(this.announceTime, DateTimeFormatter.ofPattern("yyyyMMddHH00"));

        LocalDateTime adjustedTime;
        int hour = parsedAnnounceTime.getHour();
        if (hour == 17 || hour == 5) {
            adjustedTime = parsedAnnounceTime.plusHours(4);
        } else {
            adjustedTime = parsedAnnounceTime.plusHours(10);
        }

        return new ShortLandForecast(
                parsedAnnounceTime,
                adjustedTime.plusHours(numEf * 12),
                regionId,
                pop,
                ta,
                rainType);
    }
}
