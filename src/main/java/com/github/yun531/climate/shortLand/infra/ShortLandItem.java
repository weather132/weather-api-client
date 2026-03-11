package com.github.yun531.climate.shortLand.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.shortLand.domain.ShortLand;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShortLandItem {
    @JsonProperty("announceTime")
    private String announceTime;

    @JsonProperty("regId")
    private String regionCode;

    @JsonProperty("numEf")
    private Integer numEf;

    @JsonProperty("rnSt")
    private Integer pop;

    @JsonProperty("ta")
    private Integer temp;

    @JsonProperty("rnYn")
    private Integer rainType;

    public ShortLand toShortLand(CityRegionCode regionCode) {
        LocalDateTime parsedAnnounceTime = parseTimeString(this.announceTime);
        LocalDateTime effectiveTime = calculateEffectiveTime(parsedAnnounceTime, this.numEf);

        return new ShortLand(
                parsedAnnounceTime,
                effectiveTime,
                regionCode.getId(),
                pop,
                temp,
                rainType
        );
    }

    private LocalDateTime calculateEffectiveTime(LocalDateTime announceTime, int numEf) {
        int announceHour = announceTime.getHour();

        if (announceHour == 17 || announceHour == 5) {
            return announceTime.plusHours(4 + 12 * numEf);
        } else {
            return announceTime.plusHours(10 + 12 * numEf);
        }
    }

    private LocalDateTime parseTimeString(String time) {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    }
}
