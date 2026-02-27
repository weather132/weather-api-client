package com.github.yun531.climate.midLand.domain;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;

import java.util.List;

public interface MidLandClient {
    List<MidLand> requestMidLands(MidAnnounceTime midAnnounceTime, List<ProvinceRegionCode> regionCodes);
}
