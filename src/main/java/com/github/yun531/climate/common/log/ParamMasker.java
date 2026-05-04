package com.github.yun531.climate.common.log;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * KMA URL 파라미터 로깅 시 민감 키 값을 마스킹.
 * 마스킹 대상 키와 일치하는 항목의 값을 "***"로 치환한 새 Map 을 반환.
 */
public final class ParamMasker {

    private static final Set<String> SENSITIVE_KEYS = Set.of("authKey", "apiKey", "serviceKey");
    private static final String MASKED = "***";

    private ParamMasker() {
    }

    public static Map<String, String> mask(Map<String, String> params) {
        Objects.requireNonNull(params, "params must not be null");

        Map<String, String> masked = new LinkedHashMap<>(params.size());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = SENSITIVE_KEYS.contains(entry.getKey()) ? MASKED : entry.getValue();
            masked.put(entry.getKey(), value);
        }
        return masked;
    }
}