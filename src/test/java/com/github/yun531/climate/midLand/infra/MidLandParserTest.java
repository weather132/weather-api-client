package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.common.parseConfig.ParseConfig;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
    void 정상적인_JSON파일_파싱() throws IOException {
        // given
        ClassPathResource resource = new ClassPathResource("mid-land-testcase.json");
        String rawJson = new String(resource.getInputStream().readAllBytes());
        when(mockRegionCode.getId()).thenReturn(1L);

        // when
        List<MidLand> actual = parser.parse(rawJson,
                new MidAnnounceTime(LocalDateTime.of(2026, 3, 5, 12, 0)),
                mockRegionCode);

        // then
        assertThat(actual).hasSize(9);
    }
}