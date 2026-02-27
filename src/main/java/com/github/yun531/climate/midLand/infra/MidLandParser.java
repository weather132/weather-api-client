package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCodeRepository;
import com.jayway.jsonpath.JsonPath;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MidLandParser {
    private final ParseConfig parseConfig;
    private final ProvinceRegionCodeRepository regionCodeRepository;

    public MidLandParser(ParseConfig parseConfig, ProvinceRegionCodeRepository regionCodeRepository) {
        this.parseConfig = parseConfig;
        this.regionCodeRepository = regionCodeRepository;
    }

    public List<MidLand> parse(String raw, MidAnnounceTime announceTime) {
        MidLandItem item = JsonPath.using(parseConfig.getConfiguration()).parse(raw).read("$.response.body.items.item[0]", MidLandItem.class);
        return item.toMidLands(announceTime, regionCodeRepository.findByRegionCode((item.getRegId())));
    }
}
