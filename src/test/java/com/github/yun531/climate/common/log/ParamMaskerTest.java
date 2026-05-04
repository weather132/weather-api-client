package com.github.yun531.climate.common.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParamMaskerTest {

    @Nested
    @DisplayName("마스킹 로직")
    class MaskingLogic {

        @Test
        @DisplayName("authKey 값의 별표(***) 처리")
        void authKey_마스킹_결과() {
            Map<String, String> params = Map.of(
                    "authKey", "ABCDEFGH1234",
                    "regId", "11B10101"
            );

            Map<String, String> result = ParamMasker.mask(params);

            assertThat(result).containsEntry("authKey", "***");
            assertThat(result).containsEntry("regId", "11B10101");
        }

        @Test
        @DisplayName("apiKey 값의 별표(***) 처리")
        void apiKey_마스킹_결과() {
            Map<String, String> params = Map.of(
                    "apiKey", "ABCDEFGH1234",
                    "regId", "11B10101"
            );

            Map<String, String> result = ParamMasker.mask(params);

            assertThat(result).containsEntry("apiKey", "***");
            assertThat(result).containsEntry("regId", "11B10101");
        }

        @Test
        @DisplayName("serviceKey 값의 별표(***) 처리")
        void serviceKey_마스킹_결과() {
            Map<String, String> params = Map.of(
                    "serviceKey", "ABCDEFGH1234",
                    "regId", "11B10101"
            );

            Map<String, String> result = ParamMasker.mask(params);

            assertThat(result).containsEntry("serviceKey", "***");
            assertThat(result).containsEntry("regId", "11B10101");
        }

        @Test
        @DisplayName("복수 개의 민감 키가 포함된 경우의 전체 마스킹")
        void 다중_민감_키_동시_마스킹_결과() {
            Map<String, String> params = Map.of(
                    "authKey", "AUTH1234",
                    "apiKey", "API5678",
                    "serviceKey", "SVC9012"
            );

            Map<String, String> result = ParamMasker.mask(params);

            assertThat(result)
                    .containsEntry("authKey", "***")
                    .containsEntry("apiKey", "***")
                    .containsEntry("serviceKey", "***");
        }
    }

    @Nested
    @DisplayName("데이터 무결성 및 순서")
    class IntegrityAndOrder {

        @Test
        @DisplayName("마스킹 대상이 아닌 파라미터의 평문 상태 유지")
        void 비민감_파라미터_원본_유지() {
            Map<String, String> params = Map.of(
                    "pageNo", "1",
                    "numOfRows", "9",
                    "dataType", "JSON",
                    "regId", "11B10101",
                    "tmFc", "202604291700"
            );

            Map<String, String> result = ParamMasker.mask(params);

            assertThat(result).isEqualTo(params);
        }

        @Test
        @DisplayName("입력 Map 항목의 정렬 순서 보존")
        void 입력_순서_동일_여부() {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("pageNo", "1");
            params.put("authKey", "ABCDEFGH1234");
            params.put("regId", "11B10101");
            params.put("tmFc", "202604291700");

            Map<String, String> result = ParamMasker.mask(params);

            assertThat(result.keySet())
                    .containsExactlyElementsOf(List.of("pageNo", "authKey", "regId", "tmFc"));
        }

        @Test
        @DisplayName("키 이름 대소문자 구분을 통한 정확한 매칭")
        void 대소문자_구분_매칭_결과() {
            Map<String, String> params = Map.of(
                    "AuthKey", "ABCDEFGH1234",
                    "AUTHKEY", "ABCDEFGH5678"
            );

            Map<String, String> result = ParamMasker.mask(params);

            assertThat(result)
                    .containsEntry("AuthKey", "ABCDEFGH1234")
                    .containsEntry("AUTHKEY", "ABCDEFGH5678");
        }
    }

    @Nested
    @DisplayName("예외 및 경계값 처리")
    class EdgeCases {

        @Test
        @DisplayName("빈 Map 입력 시의 빈 Map 반환")
        void 빈_Map_입력_결과() {
            Map<String, String> result = ParamMasker.mask(Map.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null 입력 시 NullPointerException 발생")
        void null_입력_예외_발생() {
            assertThatThrownBy(() -> ParamMasker.mask(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("params must not be null");
        }
    }
}