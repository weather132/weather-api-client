package com.github.yun531.climate.cityRegionCode.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "city_region_code")
@Getter
@Access(AccessType.FIELD)
public class CityRegionCode {

    @Id
    private Long id;

    private String regionCode;
    private Integer x;
    private Integer y;
    private Long provinceRegionCodeId;

    protected CityRegionCode() {}
}
