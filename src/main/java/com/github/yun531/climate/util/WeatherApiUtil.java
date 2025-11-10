package com.github.yun531.climate.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeatherApiUtil {
    public static String getLatestAnnounceTime() {
        LocalDateTime nowDateTime = LocalDateTime.now();

        int nowHour = nowDateTime.getHour();
        String latestAnnounceHourStr = hourTo2digitHour(nowHourToLatestAnnounceHour(nowHour));

        return nowDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + latestAnnounceHourStr;
    }

    private static int nowHourToLatestAnnounceHour(int nowHour) {
        final int[] announceTime = {2, 5, 8, 11, 14, 17, 20, 23};
        return Arrays.stream(announceTime).filter((h) -> h <= nowHour)
                .max()
                .orElse(23);
    }

    private static String hourTo2digitHour(int hour) {
        final List<Integer> oneDigitHours = Arrays.asList(2, 5, 8);

        String hourStr = Integer.toString(hour);
        return oneDigitHours.contains(hour) ? "0" + hourStr : hourStr;
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
}
