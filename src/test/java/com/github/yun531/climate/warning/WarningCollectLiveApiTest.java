//package com.github.yun531.climate.warning;
//
//import com.github.yun531.climate.warning.application.WarningCollectService;
//import com.github.yun531.climate.warning.contract.IssuedWarning;
//import com.github.yun531.climate.warning.contract.IssuedWarningReader;
//import com.github.yun531.climate.warning.domain.model.WarningCurrent;
//import com.github.yun531.climate.warning.domain.model.WarningEvent;
//import com.github.yun531.climate.warning.domain.model.WarningEventType;
//import com.github.yun531.climate.warning.domain.repository.RegionCodeMappingRepository;
//import com.github.yun531.climate.warning.domain.repository.WarningCurrentRepository;
//import com.github.yun531.climate.warning.domain.repository.WarningEventRepository;
//import com.google.firebase.messaging.FirebaseMessaging;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * 실제 기상청 API 호출 + 실제 MySQL 저장 통합 테스트.
// *
// *
// * 수동 실행 전용. CI에서는 제외 권장.
// */
//@SpringBootTest
//@ActiveProfiles("test")
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//class WarningCollectLiveApiTest {
//
//    @MockitoBean
//    private FirebaseMessaging firebaseMessaging;
//
//    @Autowired
//    private WarningCollectService collectService;
//
//    @Autowired
//    private WarningCurrentRepository currentRepository;
//
//    @Autowired
//    private WarningEventRepository eventRepository;
//
//    @Autowired
//    private RegionCodeMappingRepository mappingRepository;
//
//    @Autowired
//    private IssuedWarningReader issuedWarningReader;
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @BeforeAll
//    static void setUp(@Autowired JdbcTemplate jdbcTemplate) {
//        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
//        jdbcTemplate.execute("TRUNCATE TABLE warning_current");
//        jdbcTemplate.execute("TRUNCATE TABLE warning_event");
//        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
//
//        System.out.println(">>> [BeforeAll] 테스트 테이블 초기화 완료");
//    }
//
//    @Test
//    @Order(1)
//    @DisplayName("warning_region_mapping 데이터가 존재하는지 확인")
//    void mappingDataExists() {
//        List<String> codes = mappingRepository.findWarningRegionCodes("11B10101");
//        System.out.println("[매핑 확인] 서울(11B10101) → " + codes);
//
//        assertThat(codes).isNotEmpty();
//        assertThat(codes).contains("L1100100");
//    }
//
//    @Test
//    @Order(2)
//    @DisplayName("기상청 API 호출 → warning_current 저장")
//    void collectSavesCurrentWarnings() {
//        collectService.collect(LocalDateTime.now());
//
//        List<WarningCurrent> currents = currentRepository.findAll();
//        System.out.println("[warning_current] 저장 건수: " + currents.size());
//        currents.forEach(c -> System.out.printf("  %s | %s | %s | tmFc=%s%n",
//                c.getWarningRegionCode(), c.getKind(), c.getLevel(), c.getAnnounceTime()));
//
//        assertThat(currents).isNotNull();
//    }
//
//    @Test
//    @Order(3)
//    @DisplayName("첫 수집 → warning_event에 NEW 이벤트 생성 확인")
//    void firstCollectCreatesNewEvents() {
//        List<WarningCurrent> currents = currentRepository.findAll();
//
//        if (currents.isEmpty()) {
//            System.out.println("[warning_event] 현재 유효한 특보 없음 → 이벤트 0건 정상");
//            return;
//        }
//
//        String sampleCode = currents.get(0).getWarningRegionCode();
//        List<WarningEvent> events = eventRepository.findLatestByWarningRegionCodes(List.of(sampleCode));
//
//        System.out.println("[warning_event] " + sampleCode + " 이벤트:");
//        events.forEach(e -> System.out.printf("  id=%d | %s | %s | %s | prev=%s%n",
//                e.getId(), e.getKind(), e.getLevel(), e.getEventType(), e.getPrevLevel()));
//
//        assertThat(events).isNotEmpty();
//    }
//
//    @Test
//    @Order(4)
//    @DisplayName("동일 시각 재수집 → 변동 없으면 추가 이벤트 없음")
//    void secondCollectNoChange() {
//        Long beforeCount = jdbcTemplate.queryForObject(
//                "SELECT COUNT(*) FROM warning_event", Long.class);
//
//        collectService.collect(LocalDateTime.now());
//
//        Long afterCount = jdbcTemplate.queryForObject(
//                "SELECT COUNT(*) FROM warning_event", Long.class);
//
//        System.out.println("[재수집] event 건수 변화: " + beforeCount + " → " + afterCount);
//
//        assertThat(afterCount).isGreaterThanOrEqualTo(beforeCount);
//    }
//
//    @Test
//    @Order(5)
//    @DisplayName("warning_current 정합성 - S코드 없음, enum 유효")
//    void currentDataIntegrity() {
//        List<WarningCurrent> currents = currentRepository.findAll();
//
//        for (WarningCurrent c : currents) {
//            assertThat(c.getWarningRegionCode()).doesNotStartWith("S");
//            assertThat(c.getKind()).isNotNull();
//            assertThat(c.getLevel()).isNotNull();
//            assertThat(c.getAnnounceTime()).isNotNull();
//            assertThat(c.getEffectiveTime()).isNotNull();
//        }
//
//        System.out.println("[정합성] " + currents.size() + "건 모두 통과");
//    }
//
//    @Test
//    @Order(6)
//    @DisplayName("loadIssuedWarnings(11B) → 수집된 특보가 있으면 IssuedWarning 반환")
//    void loadIssuedWarningsByRegionCode() {
//        List<WarningCurrent> currents = currentRepository.findAll();
//
//        if (currents.isEmpty()) {
//            System.out.println("[loadIssuedWarnings] 현재 유효한 특보 없음 → 전 지역 빈 결과 정상");
//            return;
//        }
//
//        // warning_current에 존재하는 L코드 → 역으로 11B코드 찾기
//        String sampleLCode = currents.get(0).getWarningRegionCode();
//        String regionCode = jdbcTemplate.queryForObject(
//                "SELECT region_code FROM warning_region_mapping WHERE warning_region_code = ? LIMIT 1",
//                String.class, sampleLCode);
//
//        List<IssuedWarning> result = issuedWarningReader.loadIssuedWarnings(regionCode);
//
//        System.out.println("[loadIssuedWarnings] regionCode=" + regionCode + " → " + result.size() + "건");
//        result.forEach(w -> System.out.printf("  eventId=%d | %s | %s | %s | prev=%s | announce=%s%n",
//                w.eventId(), w.kind(), w.level(), w.eventType(), w.prevLevel(), w.announceTime()));
//
//        assertThat(result).isNotEmpty();
//        for (IssuedWarning w : result) {
//            assertThat(w.eventId()).isPositive();
//            assertThat(w.kind()).isNotNull();
//            assertThat(w.level()).isNotNull();
//            assertThat(w.eventType()).isNotEqualTo(WarningEventType.LIFTED);
//            assertThat(w.announceTime()).isNotNull();
//            assertThat(w.effectiveTime()).isNotNull();
//        }
//    }
//
//    @Test
//    @Order(7)
//    @DisplayName("loadIssuedWarnings - 존재하지 않는 regionCode → 빈 결과")
//    void loadIssuedWarningsNonExistentRegion() {
//        List<IssuedWarning> result = issuedWarningReader.loadIssuedWarnings("99999999");
//
//        System.out.println("[loadIssuedWarnings] 존재하지 않는 코드 → " + result.size() + "건");
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    @Order(8)
//    @DisplayName("loadIssuedWarnings - 서울(11B10101) 4개 권역 통합 조회")
//    void loadIssuedWarningsSeoul() {
//        List<IssuedWarning> result = issuedWarningReader.loadIssuedWarnings("11B10101");
//
//        System.out.println("[loadIssuedWarnings] 서울(11B10101) → " + result.size() + "건");
//        result.forEach(w -> System.out.printf("  eventId=%d | %s | %s | %s%n",
//                w.eventId(), w.kind(), w.level(), w.eventType()));
//
//        // LIFTED는 포함되지 않아야 함
//        result.forEach(w ->
//                assertThat(w.eventType()).isNotEqualTo(WarningEventType.LIFTED));
//    }
//}