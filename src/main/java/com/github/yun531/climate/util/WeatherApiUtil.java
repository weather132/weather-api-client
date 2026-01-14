package com.github.yun531.climate.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yun531.climate.dto.*;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

// TODO : 기상청 DB에 데이터가 없는 이유로 jsonPath 에러가 나는 경우 예외처리

public class WeatherApiUtil {

    public static LocalDateTime getShortTermLatestAnnounceTime(LocalDateTime nowDateTime) {
        int pastHours = pastHoursSinceLatestShortTermAnnouncement(nowDateTime.getHour());
        return nowDateTime.minusHours(pastHours).withMinute(0).withSecond(0).withNano(0);
    }

    public static LocalDateTime getMidTermLatestAnnounceTime(LocalDateTime nowDateTime) {
        int pastHours = pastHoursSinceLatestMidTermAnnouncement(nowDateTime.getHour());

        return nowDateTime.minusHours(pastHours).withMinute(0).withSecond(0).withNano(0);
    }

    public static String formatToShortTermTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
    }

    public static String formatToMidTermTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH00"));
    }

    public static List<Integer> parseGridData(String responseBody) {
        return Arrays.stream(responseBody.replace("\n", "").replace(" ", "").split(","))
                .map(Float::parseFloat)
                .map(Float::intValue)
                .toList();
    }

    public static TempForecastResponseItem parseTempForecast(String json) {
        Configuration config = Configuration.builder()
                .mappingProvider(new JacksonMappingProvider(new ObjectMapper()))
                .build();

        return JsonPath.using(config).parse(json).read("$.response.body.items.item[0]", TempForecastResponseItem.class);
    }

    public static LandForecastResponseItem parseLandForecast(String json) {
        Configuration config = Configuration.builder()
                .mappingProvider(new JacksonMappingProvider(new ObjectMapper()))
                .build();

        return JsonPath.using(config).parse(json).read("$.response.body.items.item[0]", LandForecastResponseItem.class);
    }

    public static List<ShortLandForecastItem> parseShortLandForecast(String json) {
        Configuration config = Configuration.builder()
                .mappingProvider(new JacksonMappingProvider(new ObjectMapper()))
                .build();

        return JsonPath.using(config).parse(json).read("$.response.body.items.item", new TypeRef<>() {
        });
    }


    // 단기예보 발표시간 : 2, 5, 8, 11, 14, 17, 20, 23시 (3시간 간격)
    private static int pastHoursSinceLatestShortTermAnnouncement(int nowHour) {
        return (nowHour + 1) % 3;
    }

    // 중기예보 발표시간 : 6, 18시 (12시간 간격)
    private static int pastHoursSinceLatestMidTermAnnouncement(int nowHour) {
        return (nowHour + 6) % 12;
    }
}
