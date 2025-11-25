package com.github.yun531.climate.dto;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class TempForecastResponseItem {
    private String regid;
    private Integer taMin3;
    private Integer taMax3;
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

        if (taMin3 != null && taMax3 != null) {
            tempList.add(new Temperature(regid, announceTime, announceTime.plusDays(3).withHour(STANDARD_HOUR), taMin3, taMax3));
        }

        tempList.add(new Temperature(regid, announceTime, announceTime.plusDays(4).withHour(STANDARD_HOUR), taMin4, taMax4));
        tempList.add(new Temperature(regid, announceTime, announceTime.plusDays(5).withHour(STANDARD_HOUR), taMin5, taMax5));
        tempList.add(new Temperature(regid, announceTime, announceTime.plusDays(6).withHour(STANDARD_HOUR), taMin6, taMax6));
        tempList.add(new Temperature(regid, announceTime, announceTime.plusDays(7).withHour(STANDARD_HOUR), taMin7, taMax7));
        tempList.add(new Temperature(regid, announceTime, announceTime.plusDays(8).withHour(STANDARD_HOUR), taMin8, taMax8));
        tempList.add(new Temperature(regid, announceTime, announceTime.plusDays(9).withHour(STANDARD_HOUR), taMin9, taMax9));
        tempList.add(new Temperature(regid, announceTime, announceTime.plusDays(10).withHour(STANDARD_HOUR), taMin10, taMax10));

        return tempList;
    }
}
