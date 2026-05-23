package com.github.yun531.climate.airQuality.infra.remote;

import com.github.yun531.climate.airQuality.domain.AirQuality;
import com.github.yun531.climate.airQuality.domain.PmItemCode;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.TypeRef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AirQualityParser {

    private static final String SUCCESS_CODE = "00";
    private static final String ITEMS_PATH = "$.response.body.items";
    private static final String RESULT_CODE_PATH = "$.response.header.resultCode";

    private final ParseConfig parseConfig;
    private final SidoRegionCodeCache sidoCache;

    public AirQualityParser(ParseConfig parseConfig, SidoRegionCodeCache sidoCache) {
        this.parseConfig = parseConfig;
        this.sidoCache = sidoCache;
    }

    public List<AirQuality> parse(String raw, PmItemCode itemCode) {
        verifySuccess(raw);
        return toMeasurements(raw, itemCode);
    }

    private void verifySuccess(String raw) {
        String resultCode = JsonPath.using(parseConfig.getConfiguration())
                .parse(raw)
                .read(RESULT_CODE_PATH, String.class);
        if (!SUCCESS_CODE.equals(resultCode)) {
            throw new IllegalStateException("AirKorea 응답 실패. resultCode=" + resultCode);
        }
    }

    private List<AirQuality> toMeasurements(String raw, PmItemCode itemCode) {
        List<AirKoreaItem> items = readItems(raw);
        return items.stream()
                .flatMap(item -> item.toMeasurements(itemCode, sidoCache).stream())
                .toList();
    }

    private List<AirKoreaItem> readItems(String raw) {
        try {
            return JsonPath.using(parseConfig.getConfiguration())
                    .parse(raw)
                    .read(ITEMS_PATH, new TypeRef<List<AirKoreaItem>>() {});
        } catch (PathNotFoundException e) {
            log.warn("AirKorea 응답 items 없음");
            return new ArrayList<>();
        }
    }
}