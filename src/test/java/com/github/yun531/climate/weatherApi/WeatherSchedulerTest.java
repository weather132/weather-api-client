/*
   예보 데이터를 불러오는 테스트 코드
   한 번 실행 시 최대 2~3 분 정도 걸림.
   아래 코드의 주석 처리를 지우고 실행할 것.
*/

package com.github.yun531.climate.weatherApi;

import com.github.yun531.climate.entity.ProvinceRegionCode;
import com.github.yun531.climate.repository.ProvinceRegionCodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class WeatherSchedulerTest {

    @Autowired
    private WeatherScheduler scheduler;

    @Autowired
    private ProvinceRegionCodeRepository provinceRegionCodeRepository;

    @Test
    void dbCheck() {
        List<ProvinceRegionCode> all = provinceRegionCodeRepository.findAll();
        assertFalse(all.isEmpty());
    }

    @Test
    void doShortTermGrid() {
        scheduler.updateShortTermGrid();
    }

    @Test
    void doMidTerm() {
        scheduler.updateMidTerm();
    }

    @Test
    void doMidPop() {
        ReflectionTestUtils.invokeMethod(scheduler, "updateMidPop");
    }
}
