package com.github.yun531.climate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TempForecastResponseItem {
    private String regId;
    private Integer taMin4;
    private Integer taMax4;
    private Integer taMin5;
    private Integer taMax5;
    private Integer taMin6;
    private Integer taMax6;
    private Integer taMin7;
    private Integer taMax7;
    private Integer taMin8;
    private Integer taMax8;
    private Integer taMin9;
    private Integer taMax9;
    private Integer taMin10;
    private Integer taMax10;

    public List<Temperature> toTemperatureList(LocalDateTime announceTime) {
        List<Temperature> tempList = new ArrayList<>();

        final int STANDARD_HOUR = 9;

        if (taMin4 != null && taMax4 != null) {
            tempList.add(new Temperature(regId, announceTime, announceTime.plusDays(4).withHour(STANDARD_HOUR), taMax4, taMin4));
        }

        tempList.add(new Temperature(regId, announceTime, announceTime.plusDays(5).withHour(STANDARD_HOUR), taMax5, taMin5));
        tempList.add(new Temperature(regId, announceTime, announceTime.plusDays(6).withHour(STANDARD_HOUR), taMax6, taMin6));
        tempList.add(new Temperature(regId, announceTime, announceTime.plusDays(7).withHour(STANDARD_HOUR), taMax7, taMin7));
        tempList.add(new Temperature(regId, announceTime, announceTime.plusDays(8).withHour(STANDARD_HOUR), taMax8, taMin8));
        tempList.add(new Temperature(regId, announceTime, announceTime.plusDays(9).withHour(STANDARD_HOUR), taMax9, taMin9));
        tempList.add(new Temperature(regId, announceTime, announceTime.plusDays(10).withHour(STANDARD_HOUR), taMax10, taMin10));

        return tempList;
    }
}
