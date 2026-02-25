package com.github.yun531.climate.provinceRegionCode.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "province_region_code")
@Access(AccessType.FIELD)
@Getter
public class ProvinceRegionCode {
    @Id
    private Long id;
    private String regionCode;

    protected ProvinceRegionCode() {}
}
