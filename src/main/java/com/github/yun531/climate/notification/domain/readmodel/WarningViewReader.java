package com.github.yun531.climate.notification.domain.readmodel;

import java.util.List;

public interface WarningViewReader {

    List<WarningView> loadWarningViews(String regionId);
}