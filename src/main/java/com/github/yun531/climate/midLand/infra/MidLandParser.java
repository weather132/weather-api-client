package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MidLandParser {
    private final ParseConfig parseConfig;

    public MidLandParser(ParseConfig parseConfig) {
        this.parseConfig = parseConfig;
    }

    public List<MidLand> parse(String raw, MidAnnounceTime announceTime, ProvinceRegionCode regionCode) {
        try {
            return _parse(raw, announceTime, regionCode);

        } catch (PathNotFoundException e) {
            return new ArrayList<>();
        }
    }

    private List<MidLand> _parse(String raw, MidAnnounceTime announceTime, ProvinceRegionCode regionCode) {
        MidLandItem item = JsonPath.using(parseConfig.getConfiguration()).parse(raw).read("$.response.body.items.item[0]", MidLandItem.class);
        return item.toMidLands(announceTime, regionCode.getId());
    }
}
