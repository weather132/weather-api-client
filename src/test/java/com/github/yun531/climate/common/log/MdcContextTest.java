package com.github.yun531.climate.common.log;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MdcContext")
class MdcContextTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Nested
    @DisplayName("진입과 종료")
    class EnterAndExit {

        @Test
        @DisplayName("진입 시 전달된 키-값이 MDC 에 push 된다")
        void enter_pushesValues() {
            try (var ignored = MdcContext.of(Map.of("traceId", "abc123", "job", "test-job"))) {
                assertThat(MDC.get("traceId")).isEqualTo("abc123");
                assertThat(MDC.get("job")).isEqualTo("test-job");
            }
        }

        @Test
        @DisplayName("종료 시 이전 값이 없던 키는 MDC 에서 제거된다")
        void exit_removesKey_whenNoPriorValue() {
            try (var ignored = MdcContext.of(Map.of("traceId", "abc123"))) {
                // 진입 상태 확인 생략
            }
            assertThat(MDC.get("traceId")).isNull();
        }
    }

    @Nested
    @DisplayName("중첩 사용")
    class Nesting {

        @Test
        @DisplayName("내부 컨텍스트 종료 후 외부 값이 복원된다")
        void innerExit_restoresOuterValue() {
            try (var outer = MdcContext.of(Map.of("traceId", "outer"))) {
                try (var inner = MdcContext.of(Map.of("traceId", "inner"))) {
                    assertThat(MDC.get("traceId")).isEqualTo("inner");
                }
                assertThat(MDC.get("traceId")).isEqualTo("outer");
            }
            assertThat(MDC.get("traceId")).isNull();
        }

        @Test
        @DisplayName("외부와 내부가 서로 다른 키를 다룰 때 두 값이 모두 유지된다")
        void independentKeys_arePreservedAcrossNesting() {
            try (var outer = MdcContext.of(Map.of("job", "outer-job"))) {
                try (var inner = MdcContext.of(Map.of("traceId", "inner-trace"))) {
                    assertThat(MDC.get("job")).isEqualTo("outer-job");
                    assertThat(MDC.get("traceId")).isEqualTo("inner-trace");
                }
                assertThat(MDC.get("job")).isEqualTo("outer-job");
                assertThat(MDC.get("traceId")).isNull();
            }
        }
    }

    @Nested
    @DisplayName("입력 검증")
    class InputValidation {

        @Test
        @DisplayName("null Map 전달 시 NPE")
        void nullMap_throws() {
            assertThatThrownBy(() -> MdcContext.of(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 값을 포함한 Map 전달 시 NPE")
        void nullValue_throws() {
            Map<String, String> map = new HashMap<>();
            map.put("traceId", null);
            assertThatThrownBy(() -> MdcContext.of(map))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}