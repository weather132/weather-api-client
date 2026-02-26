package com.github.yun531.climate.midTerm.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MidAnnounceTime {
    private final LocalDateTime announceTime;

    public MidAnnounceTime(LocalDateTime now) {
        int pastHours = pastHoursSinceLatestMidTermAnnouncement(now.getHour());
        this.announceTime = now.minusHours(pastHours).withMinute(0).withSecond(0).withNano(0);
    }

    // 중기예보 발표시간 : 6, 18시 (12시간 간격)
    private int pastHoursSinceLatestMidTermAnnouncement(int nowHour) {
        return (nowHour + 6) % 12;
    }
}