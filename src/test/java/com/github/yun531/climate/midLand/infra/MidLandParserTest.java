package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MidLandParser.class, ParseConfig.class})
class MidLandParserTest {
    @Autowired
    MidLandParser parser;


    @Test
    void parse_정상_json() {
        // given
        String rawJson = getJsonTestCase();

        ProvinceRegionCode mockRegionCode = mock(ProvinceRegionCode.class);
        when(mockRegionCode.getId()).thenReturn(1L);

        MidAnnounceTime announceTime = new MidAnnounceTime(LocalDateTime.of(2026, 3, 5, 12, 0));

        // when
        List<MidLand> actual = parser.parse(rawJson,
                announceTime,
                mockRegionCode);

        // then
        assertThat(actual).hasSize(11);
    }

    @Test
    void parse_비정상_json() {
        // given
        String rawJson = "{}";

        ProvinceRegionCode mockRegionCode = mock(ProvinceRegionCode.class);
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
        return """
                {
                  "response": {
                    "header": {
                      "resultCode": "00",
                      "resultMsg": "NORMAL_SERVICE"
                    },
                    "body": {
                      "dataType": "JSON",
                      "items": {
                        "item": [
                          {
                            "regId": "11B00000",
                            "rnSt4Am": 10,
                            "rnSt4Pm": 20,
                            "rnSt5Am": 10,
                            "rnSt5Pm": 10,
                            "rnSt6Am": 10,
                            "rnSt6Pm": 10,
                            "rnSt7Am": 10,
                            "rnSt7Pm": 20,
                            "rnSt8": 30,
                            "rnSt9": 30,
                            "rnSt10": 30,
                            "wf4Am": "맑음",
                            "wf4Pm": "맑음",
                            "wf5Am": "맑음",
                            "wf5Pm": "맑음",
                            "wf6Am": "맑음",
                            "wf6Pm": "맑음",
                            "wf7Am": "맑음",
                            "wf7Pm": "맑음",
                            "wf8": "구름많음",
                            "wf9": "구름많음",
                            "wf10": "구름많음"
                          }
                        ]
                      },
                      "pageNo": 1,
                      "numOfRows": 1,
                      "totalCount": 1
                    }
                  }
                }""";
    }
}