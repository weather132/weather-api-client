package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.midLand.domain.MidLand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
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
}