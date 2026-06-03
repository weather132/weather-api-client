package com.github.yun531.climate.airQuality.infra.remote;

import com.github.yun531.climate.airQuality.domain.AirQuality;
import com.github.yun531.climate.airQuality.domain.PmItemCode;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AirQualityParser")
class AirQualityParserTest {

    private SidoRegionCodeCache sidoCache;
    private AirQualityParser parser;

    @BeforeEach
    void setUp() {
        sidoCache = mock(SidoRegionCodeCache.class);
        // 모든 시도 코드에 임의 id 반환 (코드 문자열 해시 기반, 충돌 무관)
        when(sidoCache.findIdByCode(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(inv -> (long) inv.getArgument(0, String.class).hashCode());
        parser = new AirQualityParser(new ParseConfig(), sidoCache);
    }

    @Nested
    @DisplayName("정상 응답")
    class Success {

        @Test
        @DisplayName("item 별 17개 시도로 펼쳐짐 -> 시각별 17건, PM10 채움")
        void parsesAllSido() {
            List<AirQuality> result = parser.parse(twoItemResponse(), PmItemCode.PM10);

            assertThat(result).hasSize(34);
            assertThat(result).allMatch(aq -> aq.getPm10() != null && aq.getPm25() == null);

            Map<LocalDateTime, List<AirQuality>> byTime = result.stream()
                    .collect(Collectors.groupingBy(AirQuality::getAnnounceTime));
            assertThat(byTime.get(LocalDateTime.of(2026, 5, 21, 11, 0))).hasSize(17);
            assertThat(byTime.get(LocalDateTime.of(2026, 5, 21, 0, 0))).hasSize(17);
        }

        @Test
        @DisplayName("dataTime 24:00 -> 다음날 00:00 변환")
        void midnight24Conversion() {
            List<AirQuality> result = parser.parse(twoItemResponse(), PmItemCode.PM10);

            // 2026-05-20 24:00 -> 2026-05-21 00:00
            assertThat(result).anyMatch(aq ->
                    aq.getAnnounceTime().equals(LocalDateTime.of(2026, 5, 21, 0, 0)));
            // 정상 표기 11:00 도 그대로 존재
            assertThat(result).anyMatch(aq ->
                    aq.getAnnounceTime().equals(LocalDateTime.of(2026, 5, 21, 11, 0)));
        }

        @Test
        @DisplayName("PM25 itemCode -> pm25 채움")
        void pm25Fills() {
            List<AirQuality> result = parser.parse(twoItemResponse(), PmItemCode.PM25);

            assertThat(result).allMatch(aq -> aq.getPm25() != null && aq.getPm10() == null);
        }
    }

    @Nested
    @DisplayName("비정상 응답")
    class Failure {

        @Test
        @DisplayName("resultCode 00 아님 -> IllegalStateException")
        void nonSuccessCode() {
            String body = """
                    {"response":{"body":{"items":[]},
                    "header":{"resultMsg":"ERROR","resultCode":"99"}}}""";

            org.assertj.core.api.Assertions
                    .assertThatThrownBy(() -> parser.parse(body, PmItemCode.PM10))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("items 없음 -> 빈 리스트")
        void noItems() {
            String body = """
                    {"response":{"body":{},
                    "header":{"resultMsg":"NORMAL_CODE","resultCode":"00"}}}""";

            assertThat(parser.parse(body, PmItemCode.PM10)).isEmpty();
        }
    }

    /** dataTime 정상(11:00) 1건 + 자정(24:00) 1건, 각 17개 시도 */
    private String twoItemResponse() {
        return """
           {
             "response": {
               "header": { "resultMsg": "NORMAL_CODE", "resultCode": "00" },
               "body": {
                 "pageNo": 1, "numOfRows": 100, "totalCount": 2,
                 "items": [
                   {
                     "dataTime": "2026-05-21 11:00", "dataGubun": "1", "itemCode": "PM10",
                     "seoul":"7", "incheon":"7", "gyeonggi":"8", "gangwon":"9", "chungbuk":"5", 
                     "chungnam":"8", "daejeon":"9", "sejong":"6", "jeonbuk":"12", "jeonnam":"13", 
                     "gwangju":"10", "gyeongbuk":"14", "gyeongnam":"18", "daegu":"18", "ulsan":"19", 
                     "busan":"25", "jeju":"20"
                   },
                   {
                     "dataTime": "2026-05-20 24:00", "dataGubun": "1", "itemCode": "PM10",
                     "seoul":"4", "incheon":"3", "gyeonggi":"4", "gangwon":"6", "chungbuk":"4", 
                     "chungnam":"5", "daejeon":"5", "sejong":"3", "jeonbuk":"8", "jeonnam":"6", 
                     "gwangju":"7", "gyeongbuk":"13", "gyeongnam":"9", "daegu":"6", "ulsan":"12", 
                     "busan":"9", "jeju":"15"
                   }
                 ]
               }
             }
           }
           """;
    }
}