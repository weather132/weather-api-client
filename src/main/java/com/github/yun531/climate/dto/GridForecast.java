package com.github.yun531.climate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GridForecast {
    private final LocalDateTime announceTime;
    private final LocalDateTime effectiveTime;
    private final String forecastCategory;
    private final List<Integer> gridData;

    public Integer getForecastValue(int x, int y) {
        int value = gridData.get(coordsToIndex(x, y));

        return isValueEmpty(value) ? null : value;
    }

    private int coordsToIndex(int x, int y) {
        final int ROW_SIZE = 149;
//      COL_SIZE = 253;

        return ROW_SIZE * y + x;
    }

    private boolean isValueEmpty(int value) {
        return value == -99;
    }
}
