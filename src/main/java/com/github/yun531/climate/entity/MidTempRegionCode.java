package com.github.yun531.climate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mid_city_region_code")
@Data
@NoArgsConstructor
public class MidTempRegionCode {

    @Id
    private Long id;

    private String regionCode;
    private Integer x;
    private Integer y;

    @ManyToOne
    private MidLandRegionCode mid_pop_region_code_id;
}
