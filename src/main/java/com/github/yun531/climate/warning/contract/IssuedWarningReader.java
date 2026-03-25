package com.github.yun531.climate.warning.contract;

import java.util.List;

public interface IssuedWarningReader {
    List<IssuedWarning> loadIssuedWarnings(String regionId);
}