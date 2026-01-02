/*
   예보 데이터를 불러오는 테스트 코드
   한 번 실행 시 최대 2~3 분 정도 걸림.
   아래 코드의 주석 처리를 지우고 실행할 것.
*/

package com.github.yun531.climate.weatherApi;

import com.github.yun531.climate.repository.ProvinceRegionCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WeatherSchedulerTest {

    @Autowired
    private WeatherScheduler scheduler;

    @Autowired
    private ProvinceRegionCodeRepository landRegionCodeRepository;

//    @Test
//    void dbCheck() {
//        List<MidLandRegionCode> all = landRegionCodeRepository.findAll();
//        assertFalse(all.isEmpty());
//    }
//
//    @Test
//    void doShortTermGrid() {
//        scheduler.updateShortTermGrid();
//    }
//
//    @Test
//    void doMidTerm() {
//        scheduler.updateMidTerm();
//    }
//
//    @Test
//    void doMidPop() {
//        ReflectionTestUtils.invokeMethod(scheduler, "updateMidPop");
//    }
}
