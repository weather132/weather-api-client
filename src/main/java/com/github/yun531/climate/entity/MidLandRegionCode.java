package com.github.yun531.climate.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mid_pop_region_code")
@Data
@NoArgsConstructor
public class MidLandRegionCode {

    @Id
    private Long id;

    private String regionCode;
}
