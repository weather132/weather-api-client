package com.github.yun531.climate.entity;

import com.github.yun531.climate.dto.CoordsForecast;
import com.github.yun531.climate.dto.GridForecast;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class ShortPop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime announceTime;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime effectiveTime;

    private Integer x;
    private Integer y;

    private Integer pop;

    public ShortPop(LocalDateTime announceTime, LocalDateTime effectiveTime, Integer x, Integer y, Integer pop) {
        this.announceTime = announceTime;
        this.effectiveTime = effectiveTime;
        this.x = x;
        this.y = y;
        this.pop = pop;
    }

    public static List<ShortPop> of(GridForecast gridForecast) {
        LocalDateTime announceTime = gridForecast.getAnnounceTime();
        LocalDateTime effectiveTime = gridForecast.getEffectiveTime();
        List<CoordsForecast> coordsForecastList = gridForecast.getCoordsForecastList();

        return coordsForecastList.stream()
                .map(coordsForecast -> new ShortPop(announceTime, effectiveTime, coordsForecast.getX(), coordsForecast.getY(), coordsForecast.getValue()))
                .toList();
    }
}
