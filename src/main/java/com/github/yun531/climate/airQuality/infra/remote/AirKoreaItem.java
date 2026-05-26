package com.github.yun531.climate.airQuality.infra.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.yun531.climate.airQuality.domain.AirQuality;
import com.github.yun531.climate.airQuality.domain.PmItemCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AirKoreaItem {

    private static final DateTimeFormatter DATA_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String MIDNIGHT_24 = "24:00";
    private static final String MIDNIGHT_00 = "00:00";
    private static final String NO_DATA_MARKER = "-";

    @JsonProperty("dataTime")
    private String dataTime;

    @JsonProperty("seoul")     private String seoul;
    @JsonProperty("busan")     private String busan;
    @JsonProperty("daegu")     private String daegu;
    @JsonProperty("incheon")   private String incheon;
    @JsonProperty("gwangju")   private String gwangju;
    @JsonProperty("daejeon")   private String daejeon;
    @JsonProperty("ulsan")     private String ulsan;
    @JsonProperty("sejong")    private String sejong;
    @JsonProperty("gyeonggi")  private String gyeonggi;
    @JsonProperty("gangwon")   private String gangwon;
    @JsonProperty("chungbuk")  private String chungbuk;
    @JsonProperty("chungnam")  private String chungnam;
    @JsonProperty("jeonbuk")   private String jeonbuk;
    @JsonProperty("jeonnam")   private String jeonnam;
    @JsonProperty("gyeongbuk") private String gyeongbuk;
    @JsonProperty("gyeongnam") private String gyeongnam;
    @JsonProperty("jeju")      private String jeju;

    public List<AirQuality> toMeasurements(PmItemCode itemCode, SidoRegionCodeCache sidoCache) {
        LocalDateTime announceTime = parseAnnounceTime();

        List<AirQuality> measurements = new ArrayList<>();
        for (RawSidoValue rawSidoValue : rawSidoValues()) {
            addMeasurement(measurements, rawSidoValue, announceTime, itemCode, sidoCache);
        }
        return measurements;
    }

    private void addMeasurement(List<AirQuality> measurements, RawSidoValue rawSidoValue,
                                LocalDateTime announceTime, PmItemCode itemCode,
                                SidoRegionCodeCache sidoCache) {
        Integer value = parseMeasured(rawSidoValue.rawValue());
        if (value == null) return;

        Long sidoId = sidoCache.findIdByCode(rawSidoValue.code());
        measurements.add(measurementOf(sidoId, announceTime, value, itemCode));
    }

    /** 응답의 17개 고정 시도 필드를 (코드, 값) 쌍으로 나열. */
    private List<RawSidoValue> rawSidoValues() {
        return List.of(
                new RawSidoValue("seoul", seoul),       new RawSidoValue("busan", busan),
                new RawSidoValue("daegu", daegu),       new RawSidoValue("incheon", incheon),
                new RawSidoValue("gwangju", gwangju),   new RawSidoValue("daejeon", daejeon),
                new RawSidoValue("ulsan", ulsan),       new RawSidoValue("sejong", sejong),
                new RawSidoValue("gyeonggi", gyeonggi), new RawSidoValue("gangwon", gangwon),
                new RawSidoValue("chungbuk", chungbuk), new RawSidoValue("chungnam", chungnam),
                new RawSidoValue("jeonbuk", jeonbuk),   new RawSidoValue("jeonnam", jeonnam),
                new RawSidoValue("gyeongbuk", gyeongbuk), new RawSidoValue("gyeongnam", gyeongnam),
                new RawSidoValue("jeju", jeju));
    }

    private Integer parseMeasured(String rawValue) {
        if (isNotMeasured(rawValue)) return null;
        int parsed = Integer.parseInt(rawValue.trim());
        return parsed < 0 ? null : parsed;
    }

    private boolean isNotMeasured(String value) {
        return value == null || value.isBlank() || NO_DATA_MARKER.equals(value.trim());
    }

    private AirQuality measurementOf(Long sidoId, LocalDateTime announceTime,
                                     int value, PmItemCode itemCode) {
        return itemCode == PmItemCode.PM10
                ? new AirQuality(sidoId, announceTime, value, null)
                : new AirQuality(sidoId, announceTime, null, value);
    }

    private LocalDateTime parseAnnounceTime() {
        String raw = dataTime.trim();
        if (raw.endsWith(MIDNIGHT_24)) {
            String rolledBack = raw.replace(MIDNIGHT_24, MIDNIGHT_00);
            return LocalDateTime.parse(rolledBack, DATA_TIME_FORMAT).plusDays(1);
        }
        return LocalDateTime.parse(raw, DATA_TIME_FORMAT);
    }

    private record RawSidoValue(String code, String rawValue) {}
}