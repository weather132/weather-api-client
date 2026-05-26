package com.github.yun531.climate.forecast.application;

import com.github.yun531.climate.forecast.domain.adjust.FcstWindowAdjuster;
import com.github.yun531.climate.forecast.domain.reader.FcstViewReader;
import com.github.yun531.climate.forecast.domain.readmodel.FcstDailyView;
import com.github.yun531.climate.forecast.domain.readmodel.FcstHourlyView;
import com.github.yun531.climate.common.time.TimeUtil;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 일기예보 조회 서비스.
 * - ForecastViewReader 에서 로드 -> WindowAdjuster 적용
 */
public class ForecastService {

    private final FcstViewReader viewReader;
    private final FcstWindowAdjuster windowAdjuster;
    private final Clock clock;

    public ForecastService(
            FcstViewReader viewReader,
            FcstWindowAdjuster windowAdjuster,
            Clock clock
    ) {
        this.viewReader = viewReader;
        this.windowAdjuster = windowAdjuster;
        this.clock = clock;
    }

    // ======================= Hourly =======================

    public FcstHourlyView getHourlyForecast(String regionId) {
        return getHourlyForecast(regionId, now());
    }

    public FcstHourlyView getHourlyForecast(String regionId, LocalDateTime now) {
        LocalDateTime effectiveNow = normalizeNow(now);

        FcstHourlyView base = viewReader.loadHourly(regionId);
        if (base == null) return null;

        return windowAdjuster.adjust(base, effectiveNow);
    }

    // ======================= Daily =======================

    public FcstDailyView getDailyForecast(String regionId) {
        return viewReader.loadDaily(regionId);
    }

    // ======================= 시간 헬퍼 =======================

    private LocalDateTime normalizeNow(LocalDateTime now) {
        return (now == null) ? now() : TimeUtil.truncateToMinutes(now);
    }

    private LocalDateTime now() {
        return TimeUtil.truncateToMinutes(LocalDateTime.now(clock));
    }
}