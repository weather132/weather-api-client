package com.github.yun531.climate.warning.domain.mapping;

import java.util.List;

public interface RegionCodeMappingRepository {
    List<String> findWarningRegionCodes(String regionCode);
}