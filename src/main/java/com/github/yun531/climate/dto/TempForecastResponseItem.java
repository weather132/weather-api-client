package com.github.yun531.climate.dto;

import lombok.AllArgsConstructor;

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

    public List<TempForecast> toTempForecastList() {
        List<TempForecast> tempForecastList = new ArrayList<>();
        if (taMin3 != null && taMax3 != null) {
            tempForecastList.add(new TempForecast(3, taMin3, taMax3));
        }
        tempForecastList.add(new TempForecast(4,  taMin4, taMax4));
        tempForecastList.add(new TempForecast(5,  taMin5, taMax5));
        tempForecastList.add(new TempForecast(6,  taMin6, taMax6));
        tempForecastList.add(new TempForecast(7,  taMin7, taMax7));
        tempForecastList.add(new TempForecast(8,  taMin8, taMax8));
        tempForecastList.add(new TempForecast(9,  taMin9, taMax9));
        tempForecastList.add(new TempForecast(10,  taMin10, taMax10));

        return tempForecastList;
    }
}
