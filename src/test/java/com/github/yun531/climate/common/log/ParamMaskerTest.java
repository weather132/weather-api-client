package com.github.yun531.climate.common.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ParamMasker")
class ParamMaskerTest {

    @Test
    @DisplayName("authKey 값이 마스킹된다")
    void authKey_isMasked() {
        Map<String, String> masked = ParamMasker.mask(Map.of("authKey", "secret-key"));
        assertThat(masked).containsEntry("authKey", "***");
    }

    @Test
    @DisplayName("apiKey 값이 마스킹된다")
    void apiKey_isMasked() {
        Map<String, String> masked = ParamMasker.mask(Map.of("apiKey", "another-secret"));
        assertThat(masked).containsEntry("apiKey", "***");
    }

    @Test
    @DisplayName("비대상 키의 값은 그대로 보존된다")
    void nonSensitiveKeys_arePreserved() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("tm", "202604301700");
        params.put("regId", "11B10101");
        params.put("authKey", "secret");

        Map<String, String> masked = ParamMasker.mask(params);

        assertThat(masked)
                .containsEntry("tm", "202604301700")
                .containsEntry("regId", "11B10101")
                .containsEntry("authKey", "***");
    }

    @Test
    @DisplayName("원본 Map 은 변경되지 않는다")
    void originalMap_isNotMutated() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("authKey", "secret");

        ParamMasker.mask(params);

        assertThat(params).containsEntry("authKey", "secret");
    }

    @Test
    @DisplayName("빈 Map 은 빈 Map 을 반환한다")
    void emptyMap_returnsEmpty() {
        assertThat(ParamMasker.mask(Map.of())).isEmpty();
    }

    @Test
    @DisplayName("null Map 전달 시 NPE")
    void nullMap_throws() {
        assertThatThrownBy(() -> ParamMasker.mask(null))
                .isInstanceOf(NullPointerException.class);
    }
}