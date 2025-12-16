package com.github.yun531.climate.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mid_temperature_region_code")
@Data
@NoArgsConstructor
public class MidTempRegionCode {

    @Id
    private Long id;

    private String regionCode;
}
