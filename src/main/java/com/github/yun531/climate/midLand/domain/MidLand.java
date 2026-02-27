package com.github.yun531.climate.midLand.domain;

import com.github.yun531.climate.common.MidAnnounceTime;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Table(name = "MidPop")
@Entity
@Access(AccessType.FIELD)
@Getter
public class MidLand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private MidAnnounceTime announceTime;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime effectiveTime;

    private Long provinceRegionCodeId;

    private Integer pop;


    public MidLand(MidAnnounceTime announceTime, LocalDateTime effectiveTime, Long provinceRegionCodeId, Integer pop) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.provinceRegionCodeId = provinceRegionCodeId;
        this.pop = pop;
    }

    protected MidLand() {}
}
