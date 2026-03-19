package com.github.yun531.climate.snapshot.hourly.application;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.cityRegionCode.domain.Coordinates;
import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import com.github.yun531.climate.shortGrid.domain.ShortGridRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class HourlyServiceTest {

    @Test
    void getSnapshot_정상() {
        // given
        String regionCodeStr = "11B10101";

        Coordinates coords = mock(Coordinates.class);
        when(coords.getX()).thenReturn(1);
        when(coords.getY()).thenReturn(1);

        CityRegionCode regionCode = mock(CityRegionCode.class);
        when(regionCode.getCoordinates()).thenReturn(coords);

        CityRegionCodeRepository regionCodeRepo = mock(CityRegionCodeRepository.class);
        when(regionCodeRepo.findByRegionCode(regionCodeStr)).thenReturn(regionCode);

        AnnounceTime announceTime = mock(AnnounceTime.class);

        LocalDateTime effectiveTime = LocalDateTime.of(2026, 3, 18, 12, 0);
        List<ShortGrid> shortGrids = List.of(
                new ShortGrid(announceTime, effectiveTime, 1, 1, 1, 1),
                new ShortGrid(announceTime, effectiveTime.plusHours(1), 1, 1, 2, 2));

        ShortGridRepository shortGridRepo = mock(ShortGridRepository.class);
        when(shortGridRepo.findByAnnounceTimeAndXAndY(announceTime, 1, 1)).thenReturn(shortGrids);

        HourlyService hourlyService = new HourlyService(regionCodeRepo, shortGridRepo);

        // when
        HourlyForecastDto actual = hourlyService.getSnapshot(announceTime, regionCodeStr);

        // then
        assertThat(actual.getCoordsX()).isEqualTo(1);
        assertThat(actual.getGridForecastData().size()).isEqualTo(2);
    }

}