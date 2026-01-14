package com.github.yun531.climate.dto;

import java.util.Objects;

public record Coordinates(int x, int y) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Coordinates that)) return false;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
