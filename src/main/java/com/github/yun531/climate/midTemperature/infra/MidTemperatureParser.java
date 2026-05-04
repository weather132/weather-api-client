package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MidTemperatureParser {
    private final ParseConfig parseConfig;

    public MidTemperatureParser(ParseConfig parseConfig) {
        this.parseConfig = parseConfig;
    }

    public List<MidTemperature> parse(String rawJson, MidAnnounceTime announceTime, CityRegionCode regionCode) {
        List<MidTemperature> result;
        try {
            result = _parse(rawJson, announceTime, regionCode);
        } catch (PathNotFoundException e) {
            result = new ArrayList<>();
        }
        if (result.isEmpty()) {
            log.warn("파싱 결과 없음. regionId={}", regionCode.getRegionCode());
        }
        return result;
    }

    private List<MidTemperature> _parse(String rawJson, MidAnnounceTime announceTime, CityRegionCode regionCode) {
        MidTempItem item = JsonPath
                .using(parseConfig.getConfiguration())
                .parse(rawJson)
                .read("$.response.body.items.item[0]", MidTempItem.class);

        return item.toMidTemperatures(announceTime, regionCode.getId());
    }
}