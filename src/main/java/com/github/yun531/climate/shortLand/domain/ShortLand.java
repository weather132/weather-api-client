package com.github.yun531.climate.shortLand.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "short_land")
@Access(AccessType.FIELD)
@Getter
public class ShortLand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime announceTime;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime effectiveTime;

    private Long cityRegionCodeId;
    private Integer pop;
    private Integer temp;
    private Integer rainType;

    public ShortLand(LocalDateTime announceTime, LocalDateTime effectiveTime, Long cityRegionCodeId, Integer pop, Integer temp, Integer rainType) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.cityRegionCodeId = cityRegionCodeId;
        this.pop = pop;
        this.temp = temp;
        this.rainType = rainType;
    }

    protected ShortLand() {}
}
