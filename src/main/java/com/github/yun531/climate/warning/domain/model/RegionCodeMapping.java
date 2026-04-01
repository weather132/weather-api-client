package com.github.yun531.climate.warning.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@IdClass(RegionCodeMappingId.class)
@Table(name = "warning_region_mapping")
public class RegionCodeMapping {

    @Id
    @Column(name = "region_code", nullable = false, length = 16)
    private String regionCode;

    @Id
    @Column(name = "warning_region_code", nullable = false, length = 16)
    private String warningRegionCode;
}