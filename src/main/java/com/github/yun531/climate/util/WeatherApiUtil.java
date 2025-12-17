package com.github.yun531.climate.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yun531.climate.dto.*;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeatherApiUtil {

    public static LocalDateTime getShortTermLatestAnnounceTime(LocalDateTime nowDateTime) {
        int pastHours = pastHoursSinceLatestShortTermAnnouncement(nowDateTime.getHour());
        return nowDateTime.minusHours(pastHours).withMinute(0).withSecond(0).withNano(0);
    }

    public static String getShortTermLatestAnnounceTimeFormatted(LocalDateTime nowDateTime) {
        return formatToShortTermTime(getMidTermLatestAnnounceTime(nowDateTime));
    }

    public static LocalDateTime getMidTermLatestAnnounceTime(LocalDateTime nowDateTime) {
        int pastHours = pastHoursSinceLatestMidTermAnnouncement(nowDateTime.getHour());

        return nowDateTime.minusHours(pastHours).withMinute(0).withSecond(0).withNano(0);
    }

    public static String getMidTermLatestAnnounceTimeFormatted(LocalDateTime nowDateTime) {
        return formatToMidTermTime(getMidTermLatestAnnounceTime(nowDateTime));
    }

    public static String formatToShortTermTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
    }

    public static String formatToMidTermTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH00"));
    }

    public static List<CoordsForecast> parseGridData(String responseBody) {
        final int ROW_SIZE = 149;
        final int COL_SIZE = 253;

        List<Integer> bodyList = Arrays.stream(responseBody.replace("\n", "").replace(" ", "").split(","))
                .map(Float::parseFloat)
                .map(Float::intValue)
                .toList();

        List<CoordsForecast> coordsForecastList = new ArrayList<>();
        for (int i = 0; i < bodyList.size(); i++) {
            int value = bodyList.get(i);
            if (isCoordsEmpty(value)) {
                continue;
            }

            coordsForecastList.add(new CoordsForecast(i % ROW_SIZE, i / ROW_SIZE, value));
        }

        return coordsForecastList;
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


    // 단기예보 발표시간 : 2, 5, 8, 11, 14, 17, 20, 23시 (3시간 간격)
    private static int pastHoursSinceLatestShortTermAnnouncement(int nowHour) {
        return (nowHour + 1) % 3;
    }

    // 중기예보 발표시간 : 6, 18시 (12시간 간격)
    private static int pastHoursSinceLatestMidTermAnnouncement(int nowHour) {
        return (nowHour + 6) % 12;
    }

    private static boolean isCoordsEmpty(int value) {
        return value == -99 || value == -999;
    }
}
