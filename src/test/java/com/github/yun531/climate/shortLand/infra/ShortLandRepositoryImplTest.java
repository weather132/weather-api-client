package com.github.yun531.climate.shortLand.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.shortLand.domain.ShortLand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Sql("/unit-test-data.sql")
class ShortLandRepositoryImplTest {
    @Autowired
    ShortLandRepositoryImpl shortLandRepository;

    @Test
    void findRecentPop_정상() {
        // given
        LocalDateTime recentAnTime = LocalDateTime.of(2026, 4, 13, 11, 0);
        LocalDateTime efTime = LocalDateTime.of(2026, 4, 13, 9, 0);
        ShortLand sl1 = new ShortLand(recentAnTime, efTime, 1L, 10, 0, 0);

        LocalDateTime pastAnTime = LocalDateTime.of(2026, 4, 12, 11, 0);
        ShortLand sl2 = new ShortLand(pastAnTime, efTime, 1L, 20, 0, 0);

        shortLandRepository.save(sl1);
        shortLandRepository.save(sl2);

        CityRegionCode cityRegion = mock(CityRegionCode.class);
        when(cityRegion.getId()).thenReturn(1L);

        // when
        Integer actual = shortLandRepository.findRecentPop(cityRegion, efTime);

        // then
        assertThat(actual).isEqualTo(10);
    }

    @Test
    void findRecentMaxTemp_정상() {
        // given
        LocalDateTime recentAnTime = LocalDateTime.of(2026, 4, 13, 11, 0);
        LocalDateTime efTime = LocalDateTime.of(2026, 4, 13, 9, 0);
        ShortLand sl1 = new ShortLand(recentAnTime, efTime, 1L, 0, 1, 0);

        LocalDateTime pastAnTime = LocalDateTime.of(2026, 4, 12, 11, 0);
        ShortLand sl2 = new ShortLand(pastAnTime, efTime, 1L, 0, 2, 0);

        shortLandRepository.save(sl1);
        shortLandRepository.save(sl2);

        CityRegionCode cityRegion = mock(CityRegionCode.class);
        when(cityRegion.getId()).thenReturn(1L);

        // when
        Integer actual = shortLandRepository.findRecentMaxTemp(cityRegion, efTime);

        // then
        assertThat(actual).isEqualTo(1);
    }

    @Test
    void findRecentMinTemp_정상() {
        // given
        LocalDateTime recentAnTime = LocalDateTime.of(2026, 4, 13, 11, 0);
        LocalDateTime efTime = LocalDateTime.of(2026, 4, 13, 21, 0);
        ShortLand sl1 = new ShortLand(recentAnTime, efTime, 1L, 0, 1, 0);

        LocalDateTime pastAnTime = LocalDateTime.of(2026, 4, 12, 11, 0);
        ShortLand sl2 = new ShortLand(pastAnTime, efTime, 1L, 0, 2, 0);

        shortLandRepository.save(sl1);
        shortLandRepository.save(sl2);

        CityRegionCode cityRegion = mock(CityRegionCode.class);
        when(cityRegion.getId()).thenReturn(1L);

        // when
        Integer actual = shortLandRepository.findRecentMaxTemp(cityRegion, efTime);

        // then
        assertThat(actual).isEqualTo(1);
    }
}