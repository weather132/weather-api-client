package com.github.yun531.climate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CoordsForecast {
    private final int x;
    private final int y;
    private final int value;

    public boolean isSameCoords(CoordsForecast coordsForecast) {
        return this.x == coordsForecast.getX() && this.y == coordsForecast.getY();
    }
}
