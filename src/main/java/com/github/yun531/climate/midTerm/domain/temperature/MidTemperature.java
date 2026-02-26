package com.github.yun531.climate.midTerm.domain.temperature;

import com.github.yun531.climate.cityRegionCode.reference.CityRegionCode;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mid_temperature")
@Getter
@Access(AccessType.FIELD)
public class MidTemperature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime announceTime;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime effectiveTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_region_code_id")
    private CityRegionCode regionCode;

    private Integer maxTemp;
    private Integer minTemp;

    public MidTemperature(LocalDateTime announceTime,
                          LocalDateTime effectiveTime,
                          CityRegionCode regionCode,
                          Integer maxTemp,
                          Integer minTemp) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.regionCode = regionCode;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
    }

    protected MidTemperature() {}
}