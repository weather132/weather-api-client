package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MidLandParser.class, ParseConfig.class})
class MidLandParserTest {
    @Autowired
    MidLandParser parser;

    @Mock
    ProvinceRegionCode mockRegionCode;

    @Test
    void 정상_JSON_파싱() throws IOException {
        // given
        String rawJson = getJsonTestCase();
        when(mockRegionCode.getId()).thenReturn(1L);
        MidAnnounceTime announceTime = new MidAnnounceTime(LocalDateTime.of(2026, 3, 5, 12, 0));

        // when
        List<MidLand> actual = parser.parse(rawJson,
                announceTime,
                mockRegionCode);

        // then
        assertThat(actual).hasSize(9);
    }

    @Test
    void 비정상_JSON_파싱() {
        // given
        String rawJson = "{}";
        when(mockRegionCode.getId()).thenReturn(1L);
        MidAnnounceTime announceTime = new MidAnnounceTime(LocalDateTime.of(2026, 3, 5, 12, 0));

        // when
        List<MidLand> actual = parser.parse(rawJson,
                announceTime,
                mockRegionCode);

        // then
        assertThat(actual).hasSize(0);
    }


    String getJsonTestCase() {
        return "{\n" +
                "  \"response\": {\n" +
                "    \"header\": {\n" +
                "      \"resultCode\": \"00\",\n" +
                "      \"resultMsg\": \"NORMAL_SERVICE\"\n" +
                "    },\n" +
                "    \"body\": {\n" +
                "      \"dataType\": \"JSON\",\n" +
                "      \"items\": {\n" +
                "        \"item\": [\n" +
                "          {\n" +
                "            \"regId\": \"11B00000\",\n" +
                "            \"rnSt4Am\": 10,\n" +
                "            \"rnSt4Pm\": 20,\n" +
                "            \"rnSt5Am\": 10,\n" +
                "            \"rnSt5Pm\": 10,\n" +
                "            \"rnSt6Am\": 10,\n" +
                "            \"rnSt6Pm\": 10,\n" +
                "            \"rnSt7Am\": 10,\n" +
                "            \"rnSt7Pm\": 20,\n" +
                "            \"rnSt8\": 30,\n" +
                "            \"rnSt9\": 30,\n" +
                "            \"rnSt10\": 30,\n" +
                "            \"wf4Am\": \"맑음\",\n" +
                "            \"wf4Pm\": \"맑음\",\n" +
                "            \"wf5Am\": \"맑음\",\n" +
                "            \"wf5Pm\": \"맑음\",\n" +
                "            \"wf6Am\": \"맑음\",\n" +
                "            \"wf6Pm\": \"맑음\",\n" +
                "            \"wf7Am\": \"맑음\",\n" +
                "            \"wf7Pm\": \"맑음\",\n" +
                "            \"wf8\": \"구름많음\",\n" +
                "            \"wf9\": \"구름많음\",\n" +
                "            \"wf10\": \"구름많음\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"pageNo\": 1,\n" +
                "      \"numOfRows\": 1,\n" +
                "      \"totalCount\": 1\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}