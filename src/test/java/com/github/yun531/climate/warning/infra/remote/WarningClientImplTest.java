package com.github.yun531.climate.warning.infra.remote;

import com.github.yun531.climate.common.apiKey.ApiKey;
import com.github.yun531.climate.common.client.WeatherClient;
import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WarningClientImpl")
class WarningClientImplTest {

    private static final String TEST_URL =
            "https://apihub.kma.go.kr/api/typ01/url/wrn_now_data.php";
    private static final String TEST_AUTH_KEY = "test-auth-key";

    @Mock
    private WeatherClient weatherClient;

    @Mock
    private WarningUrl warningUrl;

    @Mock
    private ApiKey apiKey;

    @Mock
    private WarningParser parser;

    @InjectMocks
    private WarningClientImpl warningClient;

    @Test
    @DisplayName("WeatherClient에 URL과 파라미터(fe, tm, authKey)를 전달하고 파서 결과를 그대로 반환한다")
    void requestCurrentWarnings_위임체인과_파라미터를_검증() {
        LocalDateTime tm = LocalDateTime.of(2025, 11, 15, 14, 30);
        String rawResponse = "raw-csv-data";
        List<WarningCurrent> expected = List.of();

        given(warningUrl.getUrl()).willReturn(TEST_URL);
        given(apiKey.getApiKey()).willReturn(TEST_AUTH_KEY);
        given(weatherClient.requestGet(eq(TEST_URL), anyMap())).willReturn(rawResponse);
        given(parser.parse(rawResponse)).willReturn(expected);

        List<WarningCurrent> actual = warningClient.requestCurrentWarnings(tm);

        assertThat(actual).isSameAs(expected);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(weatherClient).requestGet(eq(TEST_URL), captor.capture());

        assertThat(captor.getValue())
                .containsEntry("fe", "f")
                .containsEntry("tm", "202511151430")
                .containsEntry("authKey", TEST_AUTH_KEY);
    }

    @ParameterizedTest(name = "{0} -> tm={1}")
    @CsvSource({
            "2025-01-01T00:00, 202501010000",
            "2025-12-31T23:59, 202512312359",
            "2025-03-05T09:07, 202503050907",
            "2025-11-15T14:30, 202511151430"
    })
    @DisplayName("tm 파라미터는 yyyyMMddHHmm 포맷으로 변환 (자정, 한자리 월/일/시 포함)")
    void requestCurrentWarnings_tm_포맷팅_회귀(LocalDateTime input, String expectedTm) {
        given(warningUrl.getUrl()).willReturn(TEST_URL);
        given(apiKey.getApiKey()).willReturn(TEST_AUTH_KEY);
        given(weatherClient.requestGet(any(), anyMap())).willReturn("");
        given(parser.parse(any())).willReturn(List.of());

        warningClient.requestCurrentWarnings(input);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(weatherClient).requestGet(any(), captor.capture());

        assertThat(captor.getValue().get("tm")).isEqualTo(expectedTm);
    }
}