package com.github.yun531.climate.warning.infra.remote;

import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarningParserTest {

    private final WarningParser parser = new WarningParser();

    @Nested
    @DisplayName("빈 입력 처리")
    class EmptyInput {

        @Test
        @DisplayName("null 입력 시 빈 리스트 반환")
        void nullInput() {
            List<WarningCurrent> result = parser.parse(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("blank 입력 시 빈 리스트 반환")
        void blankInput() {
            List<WarningCurrent> result = parser.parse("   ");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("skip 처리")
    class SkipLines {

        @Test
        @DisplayName("주석 라인만 있으면 빈 리스트 반환")
        void commentLinesOnly() {
            String raw = "#START7777\n"
                    + "#-----------------------------------\n"
                    + "#  특보현황 조회\n"
                    + "#7777END\n";

            List<WarningCurrent> result = parser.parse(raw);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("해상 특보(S prefix)는 skip")
        void seaWarningSkipped() {
            String raw = "S1230000, 서해남부전해상, S1232110, 서해남부북쪽안쪽먼바다, 202603302100, 202603302300, 풍랑, 주의, 발표, 31일 오전,=";

            List<WarningCurrent> result = parser.parse(raw);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("필드 수 부족(8개 미만) 시 skip")
        void insufficientFields() {
            String raw = "L1050000, 전라남도, L1051000, 여수시, 202603302030, 202603302030, 강풍,=";

            List<WarningCurrent> result = parser.parse(raw);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("알 수 없는 특보종류 시 skip")
        void unknownKind() {
            String raw = "L1050000, 전라남도, L1051000, 여수시, 202603302030, 202603302030, 폭풍, 주의, 발표, ,=";

            List<WarningCurrent> result = parser.parse(raw);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("알 수 없는 특보수준 시 skip")
        void unknownLevel() {
            String raw = "L1050000, 전라남도, L1051000, 여수시, 202603302030, 202603302030, 강풍, 위험, 발표, ,=";

            List<WarningCurrent> result = parser.parse(raw);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("정상 파싱")
    class SuccessfulParsing {

        @Test
        @DisplayName("육상 특보 단일 라인 정상 파싱")
        void singleLandWarning() {
            String raw = "L1050000, 전라남도, L1051000, 여수시, 202603302030, 202603302030, 강풍, 주의, 발표, 31일 오전(09시~12시),=";

            List<WarningCurrent> result = parser.parse(raw);

            assertThat(result).hasSize(1);

            WarningCurrent parsed = result.get(0);
            assertThat(parsed.getWarningRegionCode()).isEqualTo("L1051000");
            assertThat(parsed.getKind()).isEqualTo(WarningKind.WIND);
            assertThat(parsed.getLevel()).isEqualTo(WarningLevel.ADVISORY);
            assertThat(parsed.getAnnounceTime()).isEqualTo(LocalDateTime.of(2026, 3, 30, 20, 30));
            assertThat(parsed.getEffectiveTime()).isEqualTo(LocalDateTime.of(2026, 3, 30, 20, 30));
        }

        @Test
        @DisplayName("혼합 입력 시 육상 특보만 파싱")
        void mixedInput() {
            String raw = "#START7777\n"
                    + "#-----------------------------------\n"
                    + "\n"
                    + "S1230000, 서해남부전해상, S1232110, 서해남부북쪽안쪽먼바다, 202603302100, 202603302300, 풍랑, 주의, 발표, 31일 오전,=\n"
                    + "L1050000, 전라남도, L1051000, 여수시, 202603302030, 202603302030, 강풍, 주의, 발표, 31일 오전(09시~12시),=\n"
                    + "L1090000, 제주도, L1090800, 제주도동부, 202603301900, 202603301900, 강풍, 주의, 발표, 31일 늦은오후(15시~18시),=\n"
                    + "#7777END\n";

            List<WarningCurrent> result = parser.parse(raw);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getWarningRegionCode()).isEqualTo("L1051000");
            assertThat(result.get(1).getWarningRegionCode()).isEqualTo("L1090800");
        }
    }
}