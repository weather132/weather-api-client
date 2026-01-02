package com.github.yun531.climate.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "province_region_code")
@Data
@NoArgsConstructor
public class ProvinceRegionCode {

    @Id
    private Long id;

    private String regionCode;
}
