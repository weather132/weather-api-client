package com.github.yun531.climate.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yun531.climate.dto.*;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherApiUtil {

    public static LocalDateTime getMidTermLatestAnnounceTime(LocalDateTime nowDateTime) {
        int pastHours = pastHoursSinceLatestMidTermAnnouncement(nowDateTime.getHour());

        return nowDateTime.minusHours(pastHours).withMinute(0).withSecond(0).withNano(0);
    }

    public static String formatToMidTermTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH00"));
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


    // 중기예보 발표시간 : 6, 18시 (12시간 간격)
    private static int pastHoursSinceLatestMidTermAnnouncement(int nowHour) {
        return (nowHour + 6) % 12;
    }
}
