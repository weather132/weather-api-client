    package com.github.yun531.climate.sidoRegionCode;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "sido_region_code")
@Getter
@Access(AccessType.FIELD)
public class SidoRegionCode {
    @Id
    private Long id;
    private String code;

    protected SidoRegionCode() {}
}