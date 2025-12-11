package com.github.yun531.climate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class ShortTemperature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime announceTime;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime effectiveTime;

    private Integer x;
    private Integer y;

    private Integer maxTemp;
    private Integer minTemp;

    public ShortTemperature(LocalDateTime announceTime, LocalDateTime effectiveTime, Integer x, Integer y, Integer maxTemp, Integer minTemp) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.x = x;
        this.y = y;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
    }
}
