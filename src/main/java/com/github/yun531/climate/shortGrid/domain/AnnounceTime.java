package com.github.yun531.climate.shortGrid.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Embeddable
public class AnnounceTime {
    @Getter
    @Column(name = "announce_time", columnDefinition = "DATETIME")
    private LocalDateTime time;

    public AnnounceTime(LocalDateTime nowTime) {
        int pastHours = calculatePastHours(nowTime.getHour());
        this.time = nowTime.minusHours(pastHours).withMinute(0).withSecond(0).withNano(0);
    }

    public String formatIso() {
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    protected AnnounceTime() {}

    // 단기예보 발표시간 : 2, 5, 8, 11, 14, 17, 20, 23시 (3시간 간격)
    private int calculatePastHours(int nowHour) {
        return (nowHour + 1) % 3;
    }
}
