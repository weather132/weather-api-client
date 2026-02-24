package com.github.yun531.climate.shortGrid.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDateTime;

@Embeddable
public class AnnounceTime {
    @Getter
    private LocalDateTime announceTime;

    public AnnounceTime(LocalDateTime nowTime) {
        int pastHours = calculatePastHours(nowTime.getHour());
        this.announceTime = nowTime.minusHours(pastHours).withMinute(0).withSecond(0).withNano(0);
    }


    protected AnnounceTime() {}


    // 단기예보 발표시간 : 2, 5, 8, 11, 14, 17, 20, 23시 (3시간 간격)
    private int calculatePastHours(int nowHour) {
        return (nowHour + 1) % 3;
    }
}
