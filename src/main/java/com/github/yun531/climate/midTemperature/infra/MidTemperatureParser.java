package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MidTemperatureParser {
    private final ParseConfig parseConfig;

    public MidTemperatureParser(ParseConfig parseConfig) {
        this.parseConfig = parseConfig;
    }

    public List<MidTemperature> parse(String raw, MidAnnounceTime announceTime, CityRegionCode regionCode) {
        try {
            return _parse(raw, announceTime, regionCode);

        } catch (PathNotFoundException e) {
            return new ArrayList<>();
        }
    }

    private List<MidTemperature> _parse(String raw, MidAnnounceTime announceTime, CityRegionCode regionCode) {
        MidTempItem item = JsonPath
                .using(parseConfig.getConfiguration())
                .parse(raw)
                .read("$.response.body.items.item[0]", MidTempItem.class);

        return item.toMidTemperatures(announceTime, regionCode.getId());
    }
}
