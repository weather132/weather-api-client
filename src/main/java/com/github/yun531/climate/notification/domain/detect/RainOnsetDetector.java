package com.github.yun531.climate.notification.domain.detect;

import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.RainOnsetPayload;
import com.github.yun531.climate.notification.domain.readmodel.PopView;
import com.github.yun531.climate.common.time.TimeUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 이전/현재 POV 예보(PopView.Pair)를 비교해 "비 시작" 시각을 AlertEvent로 변환.
 */
public class RainOnsetDetector {

    private final int rainThreshold;
    private final int maxHourlyPoints;

    public RainOnsetDetector(int rainThreshold, int maxHourlyPoints) {
        this.rainThreshold = rainThreshold;
        this.maxHourlyPoints = Math.max(1, maxHourlyPoints);
    }

    public List<AlertEvent> detect(String regionId, PopView.Pair pair, LocalDateTime now) {
        if (cannotDetect(regionId, pair, now)) {
            return List.of();
        }
        return scanForRainOnsets(regionId, pair, now);
    }

    private boolean cannotDetect(String regionId, PopView.Pair pair, LocalDateTime now) {
        if (regionId == null || regionId.isBlank()) {
            return true;
        }
        if (now == null) {
            return true;
        }
        if (pair == null) {
            return true;
        }
        return pair.current() == null || pair.previous() == null;
    }

    private List<AlertEvent> scanForRainOnsets(String regionId, PopView.Pair pair, LocalDateTime now) {
        PopView curPopView = pair.current();
        LocalDateTime computedAt = computedAtOf(curPopView, now);
        Map<LocalDateTime, Integer> previousPopLookup = popLookup(pair.previous());

        List<AlertEvent> rainOnsetAlerts = new ArrayList<>(8);
        int scannedPoints = 0;
        for (PopView.Hourly.Pop hourlyPop : curPopView.hourly().pops()) {
            if (scanLimitReached(scannedPoints)) {
                break;
            }
            if (hasNoEffectiveTime(hourlyPop)) {
                continue;
            }
            scannedPoints++;
            if (isRainOnset(hourlyPop, previousPopLookup )) {
                rainOnsetAlerts.add(rainOnsetAlert(regionId, computedAt, hourlyPop));
            }
        }
        return List.copyOf(rainOnsetAlerts);
    }

    private LocalDateTime computedAtOf(PopView currentForecast, LocalDateTime now) {
        LocalDateTime source = currentForecast.announceTime() != null ? currentForecast.announceTime() : now;
        return TimeUtil.truncateToMinutes(source);
    }

    private Map<LocalDateTime, Integer> popLookup(PopView forecast) {
        Map<LocalDateTime, Integer> popByTime = new HashMap<>(PopView.HOURLY_SIZE * 2);
        for (PopView.Hourly.Pop hourlyPop : forecast.hourly().pops()) {
            if (hourlyPop == null) {
                continue;
            }
            LocalDateTime effectiveTime = hourlyPop.effectiveTime();
            if (effectiveTime == null) {
                continue;
            }
            Integer pop = hourlyPop.pop();
            if (pop == null) {
                continue;
            }
            popByTime.put(effectiveTime, pop);
        }
        return popByTime;
    }

    private AlertEvent rainOnsetAlert(String regionId, LocalDateTime computedAt, PopView.Hourly.Pop hourlyPop) {
        return new AlertEvent(
                AlertTypeEnum.RAIN_ONSET,
                regionId,
                computedAt,
                new RainOnsetPayload(hourlyPop.effectiveTime(), hourlyPop.pop()));
    }

    private boolean scanLimitReached(int scannedPoints) {
        return scannedPoints >= maxHourlyPoints;
    }

    private boolean hasNoEffectiveTime(PopView.Hourly.Pop hourlyPop) {
        return hourlyPop == null || hourlyPop.effectiveTime() == null;
    }

    private boolean isRainOnset(PopView.Hourly.Pop hourlyPop, Map<LocalDateTime, Integer> previousPopByTime) {
        Integer currentPop = hourlyPop.pop();
        if (currentPop == null) {
            return false;
        }
        Integer previousPop = previousPopByTime.get(hourlyPop.effectiveTime());
        return startedRaining(previousPop, currentPop);
    }

    /** 이전 예보가 없으면 현재 비 여부만, 있으면 '비 아님 -> 비'로의 전환만 onset으로 본다. */
    private boolean startedRaining(Integer previousPop, int currentPop) {
        if (previousPop == null) {
            return isRain(currentPop);
        }
        return !isRain(previousPop) && isRain(currentPop);
    }

    private boolean isRain(int pop) {
        return pop >= rainThreshold;
    }
}