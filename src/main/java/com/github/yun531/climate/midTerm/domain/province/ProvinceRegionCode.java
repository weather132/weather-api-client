package com.github.yun531.climate.midTerm.domain.province;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "province_region_code")
@Getter
@Access(AccessType.FIELD)
public class ProvinceRegionCode {
    @Id
    private Long id;
    private String regionCode;

    protected ProvinceRegionCode() {}
}