package com.github.yun531.climate.cityRegionCode.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

@Embeddable
@Getter
public class Coordinates {
    private Integer x;
    private Integer y;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Coordinates that)) return false;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
