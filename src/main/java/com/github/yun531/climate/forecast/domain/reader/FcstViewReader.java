package com.github.yun531.climate.forecast.domain.reader;

import com.github.yun531.climate.forecast.domain.readmodel.FcstDailyView;
import com.github.yun531.climate.forecast.domain.readmodel.FcstHourlyView;
import org.springframework.lang.Nullable;

public interface FcstViewReader {

    @Nullable
    FcstHourlyView loadHourly(String regionId);

    @Nullable
    FcstDailyView loadDaily(String regionId);
}