package com.github.yun531.climate.midTemperature.domain;

import com.github.yun531.climate.common.MidAnnounceTime;
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

    @Embedded
    private MidAnnounceTime announceTime;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime effectiveTime;

    @Column(name = "city_region_code_id")
    private Long cityRegionCodeId;

    private Integer maxTemp;
    private Integer minTemp;

    public MidTemperature(MidAnnounceTime announceTime, LocalDateTime effectiveTime, Long cityRegionCodeId, Integer maxTemp, Integer minTemp) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.cityRegionCodeId = cityRegionCodeId;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
    }

    protected MidTemperature() {}
}