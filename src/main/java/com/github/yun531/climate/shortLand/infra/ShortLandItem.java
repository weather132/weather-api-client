package com.github.yun531.climate.shortLand.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.yun531.climate.cityRegionCode.reference.CityRegionCode;
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
        LocalDateTime parsedAnnounceTime = timeStringToLocalDateTime();

        int hour = parsedAnnounceTime.getHour();
        LocalDateTime effectiveTime = getAdjustedTime(hour, parsedAnnounceTime);

        return new com.github.yun531.climate.shortLand.domain.ShortLand(
                parsedAnnounceTime,
                effectiveTime,
                regionCode.getId(),
                pop,
                temp,
                rainType
        );
    }

    private LocalDateTime getAdjustedTime(int hour, LocalDateTime parsedAnnounceTime) {
        LocalDateTime adjustedTime;
        if (hour == 17 || hour == 5) {
            adjustedTime = parsedAnnounceTime.plusHours(4);
        } else {
            adjustedTime = parsedAnnounceTime.plusHours(10);
        }

        return adjustedTime;
    }

    private LocalDateTime timeStringToLocalDateTime() {
        return LocalDateTime.parse(this.announceTime, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    }
}

// TODO : numEf 값을 사용하지 않는 것으로 보아 발효시간 계산에 오류가 있어 보임.
