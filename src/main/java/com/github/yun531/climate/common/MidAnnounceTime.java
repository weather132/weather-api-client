package com.github.yun531.climate.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Embeddable
public class MidAnnounceTime {
    @Getter
    @Column(columnDefinition = "DATETIME", name = "announce_time")
    private LocalDateTime time;

    public MidAnnounceTime(LocalDateTime nowTime) {
        this.time = nowTime.minusHours(calculatePastHour(nowTime.getHour())).withMinute(0).withSecond(0).withNano(0);
    }

    public String formatIso() {
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    protected MidAnnounceTime() {}

    // 중기예보 발표시간 : 6, 18시 (12시간 간격)
    private int calculatePastHour(int nowHour) {
        return (nowHour + 6) % 12;
    }
}
