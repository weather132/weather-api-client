package com.github.yun531.climate.util;

import com.github.yun531.climate.dto.LandForecast;
import com.github.yun531.climate.dto.LandForecastResponseItem;
import com.github.yun531.climate.dto.TempForecastResponseItem;
import com.github.yun531.climate.dto.TempForecast;
import com.jayway.jsonpath.JsonPath;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeatherApiUtil {

    public static String getShortTermLatestAnnounceTime(LocalDateTime nowDateTime) {
        int pastHours = pastHoursSinceLatestShortTermAnnouncement(nowDateTime.getHour());
        LocalDateTime latestAnnounceTime = nowDateTime.minusHours(pastHours);

        return formatToShortTermTime(latestAnnounceTime);
    }

    public static String getMidTermLatestAnnounceTime(LocalDateTime nowDateTime) {
        int pastHours = pastHoursSinceLatestMidTermAnnouncement(nowDateTime.getHour());
        LocalDateTime latestAnnounceTime = nowDateTime.minusHours(pastHours);

        return formatToMidTermTime(latestAnnounceTime);
    }

    public static String formatToShortTermTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
    }

    public static String formatToMidTermTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH00"));
    }

    public static List<List<Integer>> parseGridData(String responseBody) {
        final int ROW_SIZE = 149;
        final int COL_SIZE = 253;

        List<Integer> bodyList = Arrays.stream(responseBody.split(","))
                .map(Float::parseFloat)
                .map(Float::intValue)
                .toList();

        List<List<Integer>> gridData = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE * COL_SIZE; i += ROW_SIZE) {
            gridData.add(bodyList.subList(i, Math.min(i + ROW_SIZE, bodyList.size())));
        }

        return gridData;
    }

    public static List<TempForecast> parseTempForecast(String json) {
        TempForecastResponseItem tempForecastResponseItem = JsonPath.read(json, "$.response.body.items.item[0]");
        return tempForecastResponseItem.toTempForecastList();
    }

    public static List<LandForecast> parseLandForecast(String json) {
        LandForecastResponseItem landForecastResponseItem = JsonPath.read(json, "$.response.body.items.item[0]");
        return landForecastResponseItem.toLandForecastList();
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
