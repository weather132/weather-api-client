package com.github.yun531.climate.dto;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class LandForecastResponseItem {
    private String regid;
    private Integer rnSt4Am;
    private Integer rnSt4Pm;
    private Integer rnSt5Am;
    private Integer rnSt5Pm;
    private Integer rnSt6Am;
    private Integer rnSt6Pm;
    private Integer rnSt7Am;
    private Integer rnSt7Pm;
    private Integer rnSt8;
    private Integer rnSt9;
    private Integer rnSt10;

    public List<LandForecast> toLandForecastList() {
        List<LandForecast> landForecastList = new ArrayList<>();

        if (rnSt4Am != null && rnSt4Pm != null) {
            landForecastList.add(new LandForecast(4, rnSt4Am, rnSt4Pm));
        }
        landForecastList.add(new LandForecast(5, rnSt5Am, rnSt5Pm));
        landForecastList.add(new LandForecast(6, rnSt6Am, rnSt6Pm));
        landForecastList.add(new LandForecast(7, rnSt7Am, rnSt7Pm));
        landForecastList.add(new LandForecast(8, rnSt8, rnSt8));
        landForecastList.add(new LandForecast(9, rnSt9, rnSt9));
        landForecastList.add(new LandForecast(10, rnSt10, rnSt10));

        return landForecastList;
    }
}
