package com.github.yun531.climate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class ShortLandForecast {

    @Id
    private Long id;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime announceTime;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime effectiveTime;

    private String regionCode;
    private Integer pop;
    private Integer temp;
    private Integer rainType;

    public ShortLandForecast(LocalDateTime announceTime, LocalDateTime effectiveTime, String regionCode, Integer pop, Integer temp, Integer rainType) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.regionCode = regionCode;
        this.pop = pop;
        this.temp = temp;
        this.rainType = rainType;
    }
}
