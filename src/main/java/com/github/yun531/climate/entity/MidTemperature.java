package com.github.yun531.climate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class MidTemperature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime announceTime;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime effectiveTime;

    @ManyToOne
    @JoinColumn(name = "city_region_code_id")
    private CityRegionCode regionCode;

    private Integer maxTemp;
    private Integer minTemp;

    public MidTemperature(LocalDateTime announceTime, LocalDateTime effectiveTime, CityRegionCode regionCode, Integer maxTemp, Integer minTemp) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.regionCode = regionCode;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
    }
}
