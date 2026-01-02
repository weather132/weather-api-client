package com.github.yun531.climate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "city_region_code")
@Data
@NoArgsConstructor
public class CityRegionCode {

    @Id
    private Long id;

    private String regionCode;
    private Integer x;
    private Integer y;

    @ManyToOne
    @JoinColumn(name = "province_region_code_id")
    private ProvinceRegionCode province_region_code;
}
