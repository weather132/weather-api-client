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

    public static String getShortTermLatestAnnounceTime() {
        LocalDateTime nowDateTime = LocalDateTime.now();

        int nowHour = nowDateTime.getHour();
        String latestAnnounceHourStr = hourTo2digitHour(nowHourToShortTermLatestAnnounceHour(nowHour));

        return nowDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + latestAnnounceHourStr;
    }

    public static String getMidTermLatestAnnounceTime() {
        LocalDateTime nowDateTime = LocalDateTime.now();

        int nowHour = nowDateTime.getHour();
        String latestAnnounceHourStr = hourTo4digitHour(nowHourToMidTermLatestAnnounceHour(nowHour));

        return nowDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + latestAnnounceHourStr;
    }

    public static List<List<Float>> parseGridData(String responseBody) {
        final int ROW_SIZE = 149;
        final int COL_SIZE = 253;

        List<Float> bodyList = Arrays.stream(responseBody.split(","))
                .map(Float::parseFloat)
                .toList();

        List<List<Float>> gridData = new ArrayList<>();
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

    private static int nowHourToShortTermLatestAnnounceHour(int nowHour) {
        final int[] announceTime = {2, 5, 8, 11, 14, 17, 20, 23};
        return Arrays.stream(announceTime).filter((h) -> h <= nowHour)
                .max()
                .orElse(23);
    }

    private static String hourTo2digitHour(int hour) {
        String hourStr = Integer.toString(hour);
        return hourStr.length() == 1 ? "0" + hourStr : hourStr;
    }

    private static int nowHourToMidTermLatestAnnounceHour(int nowHour) {
        final int[] announceTime = {6, 18};
        return Arrays.stream(announceTime)
                .filter((h) -> h <= nowHour)
                .max()
                .orElse(18);
    }

    private static String hourTo4digitHour(int hour) {
        String hourStr = Integer.toString(hour);

        return hourStr.length() == 1 ? "0" + hourStr + "00" : hourStr + "00";
    }
}
