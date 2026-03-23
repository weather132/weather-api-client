package com.github.yun531.climate.snapshot.domain;

import com.github.yun531.climate.snapshot.domain.model.SnapKind;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PublishSchedulePolicy {

    private static final List<Integer> ANNOUNCE_HOURS = List.of(2, 5, 8, 11, 14, 17, 20, 23);

    private final int availableDelayMinutes;

    public PublishSchedulePolicy() {
        this(10);
    }

    public PublishSchedulePolicy(int availableDelayMinutes) {
        this.availableDelayMinutes = availableDelayMinutes;
    }

    public boolean isAccessible(LocalDateTime now, LocalDateTime announceTime) {
        return now != null
                && announceTime != null
                && !now.isBefore(announceTime.plusMinutes(availableDelayMinutes));
    }

    public LocalDateTime announceTimeFor(LocalDateTime now, SnapKind kind) {
        LocalDateTime cur = latestAvailableAnnounceTime(now);
        if (cur == null || kind == null) return null;

        return switch (kind) {
            case CURRENT -> cur;
            case PREVIOUS -> cur.minusHours(3);
        };
    }

    public LocalDateTime latestAvailableAnnounceTime(LocalDateTime now) {
        if (now == null) return null;

        LocalDateTime cutoff = now.minusMinutes(availableDelayMinutes);
        LocalDate today = cutoff.toLocalDate();

        for (int i = ANNOUNCE_HOURS.size() - 1; i >= 0; i--) {
            LocalDateTime t = today.atTime(ANNOUNCE_HOURS.get(i), 0);
            if (!t.isAfter(cutoff)) return t;
        }

        LocalDate yesterday = today.minusDays(1);
        return yesterday.atTime(ANNOUNCE_HOURS.get(ANNOUNCE_HOURS.size() - 1), 0);
    }
}