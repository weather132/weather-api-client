package com.github.yun531.climate.common.log;

import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * MDC 진입/복원을 try-with-resources 패턴으로 강제하는 헬퍼.
 * SLF4J MDC 가 ThreadLocal 기반이므로 스레드 간 격리는 자동 보장.
 */
public final class MdcContext implements AutoCloseable {

    private final Map<String, String> backup;

    private MdcContext(Map<String, String> values) {
        Objects.requireNonNull(values, "values must not be null");
        Map<String, String> snapshot = Map.copyOf(values);

        this.backup = new HashMap<>(snapshot.size());
        for (Map.Entry<String, String> entry : snapshot.entrySet()) {
            backup.put(entry.getKey(), MDC.get(entry.getKey()));
            MDC.put(entry.getKey(), entry.getValue());
        }
    }

    public static MdcContext of(Map<String, String> values) {
        return new MdcContext(values);
    }

    @Override
    public void close() {
        for (Map.Entry<String, String> entry : backup.entrySet()) {
            String previous = entry.getValue();
            if (previous == null) {
                MDC.remove(entry.getKey());
            } else {
                MDC.put(entry.getKey(), previous);
            }
        }
    }
}