package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.cityRegionCode.reference.CityRegionCodeRepository;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.jayway.jsonpath.JsonPath;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MidTemperatureParser {
    private final ParseConfig parseConfig;
    private final CityRegionCodeRepository cityRegionCodeRepository;

    public MidTemperatureParser(ParseConfig parseConfig, CityRegionCodeRepository cityRegionCodeRepository) {
        this.parseConfig = parseConfig;
        this.cityRegionCodeRepository = cityRegionCodeRepository;
    }

    public List<MidTemperature> parse(String raw, MidAnnounceTime announceTime) {
        MidTempItem item = JsonPath
                .using(parseConfig.getConfiguration())
                .parse(raw)
                .read("$.response.body.items.item[0]", MidTempItem.class);

        return item.toMidTemperatures(announceTime, cityRegionCodeRepository.findByRegionCode((item.getRegId())));

    }
}
