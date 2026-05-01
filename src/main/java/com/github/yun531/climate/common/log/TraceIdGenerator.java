package com.github.yun531.climate.common.log;

import java.util.UUID;

/**
 * 잡 사이클 식별용 traceId 생성기.
 * UUID v4 의 hex 12자리 prefix 를 사용.
 */
public final class TraceIdGenerator {

    private TraceIdGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}