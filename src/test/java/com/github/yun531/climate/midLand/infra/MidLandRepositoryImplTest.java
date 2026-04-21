package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/unit-test-data.sql")
@Transactional
class MidLandRepositoryImplTest {
    @Autowired
    MidLandRepositoryImpl midLandRepositoryImpl;

    @Test
    void saveAll_및_조회() {
        // given
        MidAnnounceTime announceTime = new MidAnnounceTime(LocalDateTime.of(2026, 3, 1, 12, 0, 0));
        LocalDateTime effectiveTime = announceTime.getTime().plusHours(1);

        List<MidLand> midLands = new ArrayList<>();
        midLands.add(new MidLand(announceTime, effectiveTime, 1L, 1));
        midLands.add(new MidLand(announceTime, effectiveTime, 2L, 2));

        // when
        midLandRepositoryImpl.saveAll(midLands);

        // then
        List<MidLand> actual = midLandRepositoryImpl.findAll();
        assertEquals(1, actual.get(0).getPop());
        assertEquals(2, actual.get(1).getPop());
    }

    @Test
    void findRecentAll_정상() {
        // given
        LocalDateTime ef1 = LocalDateTime.of(2026, 4, 21, 9, 0);

        MidLand ml1Past = new MidLand(
                new MidAnnounceTime(LocalDateTime.of(2026, 4, 15, 6, 0)),
                ef1,
                1L,
                10);

        MidLand ml1Recent = new MidLand(
                new MidAnnounceTime(LocalDateTime.of(2026, 4, 16, 6, 0)),
                ef1,
                1L,
                20);


        LocalDateTime ef2 = LocalDateTime.of(2026, 4, 21, 21, 0);

        MidLand ml2Past = new MidLand(
                new MidAnnounceTime(LocalDateTime.of(2026, 4, 15, 6, 0)),
                ef2,
                1L,
                30);

        MidLand ml2Recent = new MidLand(
                new MidAnnounceTime(LocalDateTime.of(2026, 4, 16, 6, 0)),
                ef2,
                1L,
                40);

        ProvinceRegionCode province = mock(ProvinceRegionCode.class);
        when(province.getId()).thenReturn(1L);

        midLandRepositoryImpl.saveAll(List.of(ml1Past, ml1Recent, ml2Past, ml2Recent));

        // when
        Map<LocalDateTime, MidLand> actual = midLandRepositoryImpl.findRecentAll(province, List.of(ef1, ef2));

        // then
        assertThat(actual.get(ef1).getPop()).isEqualTo(20);
        assertThat(actual.get(ef2).getPop()).isEqualTo(40);
    }

    @Test
    void findRecentAll_빈_발효시간() {
        // given
        ProvinceRegionCode province = mock(ProvinceRegionCode.class);
        when(province.getId()).thenReturn(1L);

        // when
        Map<LocalDateTime, MidLand> actual = midLandRepositoryImpl.findRecentAll(province, List.of());

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void findRecentAll_null_발효시간() {
        // given
        ProvinceRegionCode province = mock(ProvinceRegionCode.class);
        when(province.getId()).thenReturn(1L);

        // when
        Map<LocalDateTime, MidLand> actual = midLandRepositoryImpl.findRecentAll(province, null);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void findRecent_정상() {
        // given
        LocalDateTime ef1 = LocalDateTime.of(2026, 4, 21, 9, 0);

        MidLand ml1Past = new MidLand(
                new MidAnnounceTime(LocalDateTime.of(2026, 4, 15, 6, 0)),
                ef1,
                1L,
                10);

        MidLand ml1Recent = new MidLand(
                new MidAnnounceTime(LocalDateTime.of(2026, 4, 16, 6, 0)),
                ef1,
                1L,
                20);

        midLandRepositoryImpl.saveAll(List.of(ml1Past, ml1Recent));

        ProvinceRegionCode province = mock(ProvinceRegionCode.class);
        when(province.getId()).thenReturn(1L);

        // when
        MidLand actual = midLandRepositoryImpl.findRecent(province, ef1);

        // then
        assertThat(actual.getPop()).isEqualTo(20);
    }

    @Test
    void findById_정상() {
        // given
        MidLand ml = new MidLand(new MidAnnounceTime(LocalDateTime.of(2026, 4, 21, 6, 0)),
                LocalDateTime.of(2026, 4, 25, 9, 0),
                1L,
                10);

        midLandRepositoryImpl.saveAll(List.of(ml));
        Long id = midLandRepositoryImpl.findAll().get(0).getId();

        // when
        MidLand actual = midLandRepositoryImpl.findById(id);

        // then
        assertThat(actual.getPop()).isEqualTo(10);
    }

    @Test
    void findById_존재하지_않는_id_조회() {
        assertThrows(NoSuchElementException.class, () -> midLandRepositoryImpl.findById(1L));
    }
}