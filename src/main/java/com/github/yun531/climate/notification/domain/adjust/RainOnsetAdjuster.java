package com.github.yun531.climate.notification.domain.adjust;

import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.payload.RainOnsetPayload;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * RainOnset AlertEvent를 now 기준 effectiveTime 윈도우로 필터링.
 * - window: [nowHour + startOffset, nowHour + endOffset]
 * - 윈도우 밖 이벤트는 제거하고, occurredAt은 nowHour로 통일.
 */
public class RainOnsetAdjuster {

    private static final Comparator<AlertEvent> BY_EFFECTIVE_TIME = Comparator.comparing(
            event -> ((RainOnsetPayload) event.payload()).effectiveTime(),
            Comparator.nullsLast(Comparator.naturalOrder()));

    private final int maxOffsetHours;   // 기본 24
    private final int startOffsetHours; // 기본 1 (now+1부터)

    public RainOnsetAdjuster(int maxOffsetHours) {
        this(maxOffsetHours, 1);
    }

    public RainOnsetAdjuster(int maxOffsetHours, int startOffsetHours) {
        this.maxOffsetHours = Math.max(0, maxOffsetHours);
        this.startOffsetHours = Math.max(0, startOffsetHours);
    }

    /** withinHours로 윈도우를 축소할 수 있다. null 이면 maxOffsetHours를 사용한다. */
    public List<AlertEvent> adjust(List<AlertEvent> events, LocalDateTime now, @Nullable Integer withinHours) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        if (now == null) {
            return List.copyOf(events);
        }

        int endOffset = resolveEndOffset(withinHours);
        if (endOffset < startOffsetHours) {
            return List.of();
        }

        List<AlertEvent> windowedEvents = collectWithinWindow(events, now, endOffset);
        if (windowedEvents.isEmpty()) {
            return List.of();
        }

        windowedEvents.sort(BY_EFFECTIVE_TIME);
        return limitToWindowSize(windowedEvents, endOffset);
    }

    private List<AlertEvent> collectWithinWindow(List<AlertEvent> events, LocalDateTime now, int endOffset) {
        LocalDateTime nowHour = now.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime windowStart = nowHour.plusHours(startOffsetHours);
        LocalDateTime windowEnd = nowHour.plusHours(endOffset);

        List<AlertEvent> windowedEvents = new ArrayList<>(events.size());
        for (AlertEvent event : events) {
            RainOnsetPayload payload = rainOnsetPayloadFrom(event);
            LocalDateTime effectiveTime = payload.effectiveTime();
            if (effectiveTime == null) {
                continue;
            }
            if (isOutsideWindow(effectiveTime, windowStart, windowEnd)) {
                continue;
            }
            windowedEvents.add(adjustedEvent(event, nowHour));
        }
        return windowedEvents;
    }

    private boolean isOutsideWindow(LocalDateTime effectiveTime, LocalDateTime windowStart, LocalDateTime windowEnd) {
        return effectiveTime.isBefore(windowStart) || effectiveTime.isAfter(windowEnd);
    }

    private AlertEvent adjustedEvent(AlertEvent event, LocalDateTime nowHour) {
        return new AlertEvent(event.type(), event.regionId(), nowHour, event.payload());
    }

    private List<AlertEvent> limitToWindowSize(List<AlertEvent> kept, int endOffset) {
        int windowSize = endOffset - startOffsetHours + 1;
        if (kept.size() <= windowSize) {
            return List.copyOf(kept);
        }
        return List.copyOf(kept.subList(0, windowSize));
    }

    private RainOnsetPayload rainOnsetPayloadFrom(AlertEvent event) {
        if (event.payload() instanceof RainOnsetPayload payload) {
            return payload;
        }
        throw new IllegalArgumentException(
                "RainOnsetAdjuster expects RainOnsetPayload, got: "
                        + event.payload().getClass().getSimpleName());
    }

    /** withinHours가 있으면 maxOffsetHours와 비교해 더 작은 쪽을 사용. */
    private int resolveEndOffset(@Nullable Integer withinHours) {
        if (withinHours == null) {
            return maxOffsetHours;
        }
        return Math.min(maxOffsetHours, Math.max(0, withinHours));
    }
}