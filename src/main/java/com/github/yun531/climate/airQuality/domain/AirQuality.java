package com.github.yun531.climate.airQuality.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 시도 단위 시간대별 미세먼지 측정값.
 * pm10/pm25 중 하나는 null 허용 (둘 다 null 은 금지).
 */
@Entity
@Table(name = "air_quality")
@Access(AccessType.FIELD)
@Getter
public class AirQuality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sidoRegionCodeId;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime announceTime;

    private Integer pm10;
    private Integer pm25;

    public AirQuality(Long sidoRegionCodeId, LocalDateTime announceTime, Integer pm10, Integer pm25) {
        Objects.requireNonNull(sidoRegionCodeId, "sidoRegionCodeId must not be null");
        Objects.requireNonNull(announceTime, "announceTime must not be null");
        if (pm10 == null && pm25 == null) {
            throw new IllegalArgumentException("pm10 and pm25 must not both be null");
        }
        this.sidoRegionCodeId = sidoRegionCodeId;
        this.announceTime = announceTime;
        this.pm10 = pm10;
        this.pm25 = pm25;
    }

    protected AirQuality() {}
}