package com.github.yun531.climate.cityRegionCode.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Coordinates {
    private Integer x;
    private Integer y;
}
