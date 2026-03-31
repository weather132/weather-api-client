package com.github.yun531.climate.warning.infra.remote;

import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WarningParser {

    private static final DateTimeFormatter TM_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static final Map<String, WarningKind> KIND_MAP = Map.of(
            "호우", WarningKind.RAIN,
            "폭염", WarningKind.HEAT,
            "한파", WarningKind.COLDWAVE,
            "대설", WarningKind.HEAVY_SNOW,
            "강풍", WarningKind.WIND,
            "건조", WarningKind.DRY,
            "안개", WarningKind.FOG,
            "태풍", WarningKind.TYPHOON,
            "풍랑", WarningKind.HIGH_WAVE
    );

    private static final Map<String, WarningLevel> LEVEL_MAP = Map.of(
            "예비", WarningLevel.WATCH,
            "주의", WarningLevel.ADVISORY,
            "경보", WarningLevel.WARNING
    );

    public List<WarningCurrent> parse(String raw) {
        if (raw == null || raw.isBlank()) return List.of();

        List<WarningCurrent> results = new ArrayList<>();

        for (String line : raw.split("\n")) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("#")) continue;

            WarningCurrent parsed = parseLine(trimmed);
            if (parsed != null) {
                results.add(parsed);
            }
        }

        return results;
    }

    private WarningCurrent parseLine(String line) {
        String cleaned = line.endsWith("=") ? line.substring(0, line.length() - 1) : line;
        String[] fields = cleaned.split(",");

        if (fields.length < 8) {
            log.warn("[WarningParser] 필드 수 부족, skip: {}", line);
            return null;
        }

        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }

        String regId = fields[2];
        if (regId.startsWith("S")) return null;

        WarningKind kind = KIND_MAP.get(fields[6]);
        if (kind == null) {
            log.warn("[WarningParser] 알 수 없는 특보종류, skip: {}", fields[6]);
            return null;
        }

        WarningLevel level = LEVEL_MAP.get(fields[7]);
        if (level == null) {
            log.warn("[WarningParser] 알 수 없는 특보수준, skip: {}", fields[7]);
            return null;
        }

        LocalDateTime announceTime  = LocalDateTime.parse(fields[4], TM_FORMAT);
        LocalDateTime effectiveTime = LocalDateTime.parse(fields[5], TM_FORMAT);

        return new WarningCurrent(regId, kind, level, announceTime, effectiveTime);
    }
}