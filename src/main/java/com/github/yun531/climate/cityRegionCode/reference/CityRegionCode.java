package com.github.yun531.climate.cityRegionCode.reference;

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

    @Embedded
    private Coordinates coordinates;
    private Long provinceRegionCodeId;

    protected CityRegionCode() {}
}
