package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import com.jayway.jsonpath.JsonPath;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MidLandParser {
    private final ParseConfig parseConfig;

    public MidLandParser(ParseConfig parseConfig) {
        this.parseConfig = parseConfig;
    }

    public List<MidLand> parse(String raw, MidAnnounceTime announceTime, ProvinceRegionCode regionCode) {
        MidLandItem item = JsonPath.using(parseConfig.getConfiguration()).parse(raw).read("$.response.body.items.item[0]", MidLandItem.class);
        return item.toMidLands(announceTime, regionCode.getId());
    }
}
