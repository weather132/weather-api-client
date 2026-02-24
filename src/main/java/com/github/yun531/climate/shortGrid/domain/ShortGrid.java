package com.github.yun531.climate.shortGrid.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Table(name = "short_land")
@Entity
@Access(AccessType.FIELD)
@Getter
public class ShortGrid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private AnnounceTime announceTime;
    private LocalDateTime effectiveTime;
    private Integer x;
    private Integer y;

    private Integer pop;
    private Integer temp;

    public ShortGrid(AnnounceTime announceTime, LocalDateTime effectiveTime, Integer x, Integer y, Integer pop, Integer temp) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.x = x;
        this.y = y;
        this.pop = pop;
        this.temp = temp;
    }

    protected ShortGrid() {}
}
