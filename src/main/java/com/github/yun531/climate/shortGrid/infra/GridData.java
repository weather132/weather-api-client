package com.github.yun531.climate.shortGrid.infra;

import java.util.Arrays;
import java.util.List;

public class GridData {
    private final List<Integer> data;

    public GridData(String rawData) {
        this.data = Arrays.stream(rawData.replace("\n", "").replace(" ", "").split(","))
                .map(Float::parseFloat)
                .map(Float::intValue)
                .toList();
    }

    public Integer getData(int x, int y) {
        Integer value = data.get(coordsToIndex(x, y));
        if (value == -99) {
            return null;
        }

        return value;
    }


    // col size : 253, row size : 149
    private int coordsToIndex(int x, int y) {
        final int ROW_SIZE = 149;

        return ROW_SIZE * y + x;
    }
}
