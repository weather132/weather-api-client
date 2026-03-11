package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MidTemperatureParser.class, ParseConfig.class})
class MidTemperatureParserTest {
    @Autowired
    MidTemperatureParser parser;

    @Test
    void parse_정상_json() {
        // given
        String rawJson = getJsonTestCase();

        CityRegionCode mockRegionCode = mock(CityRegionCode.class);
        when(mockRegionCode.getId()).thenReturn(1L);

        MidAnnounceTime announceTime = new MidAnnounceTime(LocalDateTime.of(2026, 3, 5, 12, 0));

        // when
        List<MidTemperature> actual = parser.parse(rawJson,
                announceTime,
                mockRegionCode);

        // then
        assertThat(actual).hasSize(7);
    }

    @Test
    void parse_비정상_json() {
        // given
        String rawJson = "{}";

        CityRegionCode mockRegionCode = mock(CityRegionCode.class);
        when(mockRegionCode.getId()).thenReturn(1L);

        MidAnnounceTime announceTime = new MidAnnounceTime(LocalDateTime.of(2026, 3, 5, 12, 0));

        // when
        List<MidTemperature> actual = parser.parse(rawJson,
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
                            "regId": "11B10101",
                            "taMin4": 3,
                            "taMin4Low": 1,
                            "taMin4High": 1,
                            "taMax4": 12,
                            "taMax4Low": 1,
                            "taMax4High": 1,
                            "taMin5": 3,
                            "taMin5Low": 1,
                            "taMin5High": 1,
                            "taMax5": 11,
                            "taMax5Low": 1,
                            "taMax5High": 0,
                            "taMin6": 2,
                            "taMin6Low": 1,
                            "taMin6High": 2,
                            "taMax6": 12,
                            "taMax6Low": 1,
                            "taMax6High": 1,
                            "taMin7": 3,
                            "taMin7Low": 2,
                            "taMin7High": 2,
                            "taMax7": 12,
                            "taMax7Low": 1,
                            "taMax7High": 1,
                            "taMin8": 2,
                            "taMin8Low": 0,
                            "taMin8High": 1,
                            "taMax8": 12,
                            "taMax8Low": 0,
                            "taMax8High": 1,
                            "taMin9": 4,
                            "taMin9Low": 0,
                            "taMin9High": 2,
                            "taMax9": 12,
                            "taMax9Low": 0,
                            "taMax9High": 1,
                            "taMin10": 4,
                            "taMin10Low": 0,
                            "taMin10High": 2,
                            "taMax10": 13,
                            "taMax10Low": 0,
                            "taMax10High": 2
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