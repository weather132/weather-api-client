package com.github.yun531.climate.shortLand.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.shortLand.domain.ShortLand;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShortLandClientImplTest {

    @Test
    void makeParams_정상() {
        ApiKey apiKey = Mockito.mock(ApiKey.class);
        Mockito.when(apiKey.getApiKey()).thenReturn("abcde");

        CityRegionCode code = Mockito.mock(CityRegionCode.class);
        Mockito.when(code.getRegionCode()).thenReturn("11B10101");

        ShortLandClientImpl shortLandClient = new ShortLandClientImpl(apiKey, null, null, null);

        // when
        Map<String, String> actual = ReflectionTestUtils.invokeMethod(shortLandClient, "makeParams", code);

        // then
        assertThat(actual.get("pageNo")).isEqualTo("1");
        assertThat(actual.get("numOfRows")).isEqualTo("9");
        assertThat(actual.get("dataType")).isEqualTo("JSON");
        assertThat(actual.get("regId")).isEqualTo("11B10101");
        assertThat(actual.get("authKey")).isEqualTo("abcde");
    }

    @Test
    void parse_정상_json() {
        // given
        String rawJson = getJsonTestCase();

        ShortLandClientImpl shortLandClient = new ShortLandClientImpl(null, null, null, new ParseConfig());

        CityRegionCode mockRegionCode = mock(CityRegionCode.class);
        when(mockRegionCode.getId()).thenReturn(1L);

        // when
        List<ShortLand> actual = ReflectionTestUtils.invokeMethod(shortLandClient, "parse", rawJson, mockRegionCode);

        // then
        assertThat(actual).hasSize(7);
    }

    @Test
    void parse_비정상_json() {
        // given
        String rawJson = "{}";

        ShortLandClientImpl shortLandClient = new ShortLandClientImpl(null, null, null, new ParseConfig());

        CityRegionCode mockRegionCode = mock(CityRegionCode.class);
        when(mockRegionCode.getId()).thenReturn(1L);

        // when
        List<ShortLand> actual = ReflectionTestUtils.invokeMethod(shortLandClient, "parse", rawJson, mockRegionCode);

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
                            "announceTime": 202603101100,
                            "numEf": 0,
                            "regId": "11B10101",
                            "rnSt": 0,
                            "rnYn": 0,
                            "ta": "9",
                            "wd1": "W",
                            "wd2": "NW",
                            "wdTnd": "1",
                            "wf": "맑음",
                            "wfCd": "DB01",
                            "wsIt": ""
                          },
                          {
                            "announceTime": 202603101100,
                            "numEf": 1,
                            "regId": "11B10101",
                            "rnSt": 30,
                            "rnYn": 0,
                            "ta": "0",
                            "wd1": "NE",
                            "wd2": "E",
                            "wdTnd": "1",
                            "wf": "흐림",
                            "wfCd": "DB04",
                            "wsIt": ""
                          },
                          {
                            "announceTime": 202603101100,
                            "numEf": 2,
                            "regId": "11B10101",
                            "rnSt": 30,
                            "rnYn": 0,
                            "ta": "8",
                            "wd1": "W",
                            "wd2": "NW",
                            "wdTnd": "1",
                            "wf": "흐림",
                            "wfCd": "DB04",
                            "wsIt": ""
                          },
                          {
                            "announceTime": 202603101100,
                            "numEf": 3,
                            "regId": "11B10101",
                            "rnSt": 20,
                            "rnYn": 0,
                            "ta": "2",
                            "wd1": "NE",
                            "wd2": "E",
                            "wdTnd": "1",
                            "wf": "구름많음",
                            "wfCd": "DB03",
                            "wsIt": ""
                          },
                          {
                            "announceTime": 202603101100,
                            "numEf": 4,
                            "regId": "11B10101",
                            "rnSt": 10,
                            "rnYn": 0,
                            "ta": "12",
                            "wd1": "NE",
                            "wd2": "E",
                            "wdTnd": "1",
                            "wf": "맑음",
                            "wfCd": "DB01",
                            "wsIt": ""
                          },
                          {
                            "announceTime": 202603101100,
                            "numEf": 5,
                            "regId": "11B10101",
                            "rnSt": 0,
                            "rnYn": 0,
                            "ta": "1",
                            "wd1": "NE",
                            "wd2": "E",
                            "wdTnd": "1",
                            "wf": "맑음",
                            "wfCd": "DB01",
                            "wsIt": ""
                          },
                          {
                            "announceTime": 202603101100,
                            "numEf": 6,
                            "regId": "11B10101",
                            "rnSt": 0,
                            "rnYn": 0,
                            "ta": "12",
                            "wd1": "E",
                            "wd2": "SE",
                            "wdTnd": "1",
                            "wf": "맑음",
                            "wfCd": "DB01",
                            "wsIt": ""
                          }
                        ]
                      },
                      "pageNo": 1,
                      "numOfRows": 9,
                      "totalCount": 7
                    }
                  }
                }""";
    }
}