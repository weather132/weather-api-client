package com.github.yun531.climate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class ShortLand {

    @Id
    private Long id;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime announceTime;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime effectiveTime;

    @ManyToOne
    @JoinColumn(name = "city_region_code_id")
    private CityRegionCode regionCode;
    private Integer pop;
    private Integer temp;
    private Integer rainType;

    public ShortLand(LocalDateTime announceTime, LocalDateTime effectiveTime, CityRegionCode regionCode, Integer pop, Integer temp, Integer rainType) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.regionCode = regionCode;
        this.pop = pop;
        this.temp = temp;
        this.rainType = rainType;
    }
}
