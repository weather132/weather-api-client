package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/unit-test-data.sql")
@Transactional
class MidTemperatureRepositoryImplTest {
    @Autowired
    MidTemperatureRepositoryImpl midTemperatureRepositoryImpl;

    @Test
    void saveAll_및_조회() {
        // given
        MidAnnounceTime announceTime = new MidAnnounceTime(LocalDateTime.of(2026, 3, 1, 12, 0, 0));
        LocalDateTime effectiveTime = announceTime.getTime().plusHours(1);

        List<MidTemperature> midTemps = new ArrayList<>();
        midTemps.add(new MidTemperature(announceTime, effectiveTime, 1L, 1, 1));
        midTemps.add(new MidTemperature(announceTime, effectiveTime, 2L, 2, 2));

        // when
        midTemperatureRepositoryImpl.saveAll(midTemps);

        // then
        List<MidTemperature> actual = midTemperatureRepositoryImpl.findAll();
        assertEquals(1, actual.get(0).getMaxTemp());
        assertEquals(2, actual.get(1).getMaxTemp());
    }
}