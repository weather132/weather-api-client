package com.github.yun531.climate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class MidPop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime announceTime;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime effectiveTime;

    @ManyToOne
    @JoinColumn(name = "province_region_code_id")
    private ProvinceRegionCode regionCode;

    private Integer pop;

    public MidPop(LocalDateTime announceTime, LocalDateTime effectiveTime, ProvinceRegionCode regionCode, Integer pop) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.regionCode = regionCode;
        this.pop = pop;
    }
}
