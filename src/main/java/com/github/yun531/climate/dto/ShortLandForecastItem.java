package com.github.yun531.climate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.yun531.climate.entity.CityRegionCode;
import com.github.yun531.climate.entity.ShortLand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShortLandForecastItem {
    private String announceTime;

    @JsonProperty("regId")
    private String regionId;

    @JsonProperty("numEf")
    private Integer numEf;

    @JsonProperty("rnSt")
    private Integer pop;

    @JsonProperty("ta")
    private Integer temp;

    @JsonProperty("rnYn")
    private Integer rainType;

    public ShortLand toEntity(CityRegionCode regionCode) {
        LocalDateTime parsedAnnounceTime = LocalDateTime.parse(this.announceTime, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        LocalDateTime adjustedTime;
        int hour = parsedAnnounceTime.getHour();
        if (hour == 17 || hour == 5) {
            adjustedTime = parsedAnnounceTime.plusHours(4);
        } else {
            adjustedTime = parsedAnnounceTime.plusHours(10);
        }

        return new ShortLand(
                parsedAnnounceTime,
                adjustedTime.plusHours(numEf * 12),
                regionCode,
                pop,
                temp,
                rainType);
    }
}
