package com.github.yun531.climate.midTerm.domain.pop;

import com.github.yun531.climate.midTerm.domain.province.ProvinceRegionCode;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mid_pop")
@Getter
@Access(AccessType.FIELD)
public class MidPop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime announceTime;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime effectiveTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_region_code_id")
    private ProvinceRegionCode regionCode;

    private Integer pop;

    public MidPop(LocalDateTime announceTime,
                  LocalDateTime effectiveTime,
                  ProvinceRegionCode regionCode,
                  Integer pop) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.regionCode = regionCode;
        this.pop = pop;
    }

    protected MidPop() {}
}