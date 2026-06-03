package com.github.yun531.climate.integration;

import com.github.yun531.climate.airQuality.application.AirQualityCollectService;
import com.github.yun531.climate.airQuality.domain.AirQuality;
import com.github.yun531.climate.airQuality.domain.AirQualityClient;
import com.github.yun531.climate.airQuality.domain.PmItemCode;
import com.github.yun531.climate.forecast.application.AirQualityService;
import com.github.yun531.climate.forecast.domain.readmodel.AirQualityView;
import com.github.yun531.climate.forecast.infra.cache.AirQualityCacheManager;
import com.github.yun531.climate.notification.application.alert.GenerateAlertsCommand;
import com.github.yun531.climate.notification.application.alert.GenerateAlertsService;
import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.AirPollutionPayload;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers
@Import(AirQualityIntegrationTest.AirQualityTestConfig.class)
@DisplayName("AirQuality 통합 테스트")
class AirQualityIntegrationTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("climate")
                    .withUsername("test")
                    .withPassword("test")
                    .withCommand("--log-bin-trust-function-creators=1")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("schema.sql"), "/schema.sql");

    @DynamicPropertySource
    static void datasourceConfig(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @BeforeAll
    static void initSchema() throws Exception {
        var result = mysql.execInContainer(
                "sh", "-c", "mysql -u test -ptest climate < /schema.sql");
        if (result.getExitCode() != 0) {
            throw new RuntimeException("init failed: " + result.getStderr());
        }
    }

    // 모든 시각 계산의 기준 -- 고정 Clock 과 더미 데이터가 공유.
    static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 5, 21, 11, 30);
    private static final String SEOUL_REGION_ID = "11B10101";

    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbcTemplate;

    @TestConfiguration
    static class AirQualityTestConfig {
        @Bean
        @Primary
        public AirQualityClient airQualityClient() {
            return new StubAirQualityClient();
        }

        @Bean
        @Primary
        public Clock fixedClock() {
            return Clock.fixed(
                    FIXED_NOW.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        }
    }

    static class StubAirQualityClient implements AirQualityClient {
        private List<AirQuality> pm10 = List.of();
        private List<AirQuality> pm25 = List.of();

        void set(List<AirQuality> pm10, List<AirQuality> pm25) {
            this.pm10 = pm10;
            this.pm25 = pm25;
        }

        @Override
        public List<AirQuality> fetchLatest(PmItemCode itemCode) {
            return itemCode == PmItemCode.PM10 ? pm10 : pm25;
        }
    }

    private Long seoulSidoId() {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM sido_region_code WHERE code = 'seoul'", Long.class);
    }

    private void seedSido() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("TRUNCATE TABLE air_quality");
            stmt.execute("DELETE FROM city_region_code");
            stmt.execute("DELETE FROM sido_region_code");
            stmt.execute("INSERT INTO sido_region_code VALUES (null, 'seoul')");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
        Long sidoId = seoulSidoId();
        // city(11B10101) 를 seoul 시도에 매핑해 직접 삽입
        jdbcTemplate.update(
                "INSERT INTO city_region_code (region_code, x, y, sido_region_code_id) "
                        + "VALUES (?, ?, ?, ?)",
                SEOUL_REGION_ID, 60, 127, sidoId);
    }

    private void insertAirQuality(Long sidoId, LocalDateTime at, Integer pm10, Integer pm25) {
        jdbcTemplate.update(
                "INSERT INTO air_quality VALUES (null, ?, ?, ?, ?)", sidoId, at, pm10, pm25);
    }

    @Nested
    @DisplayName("쓰기 경로 -- 수집 batch INSERT")
    @TestInstance(Lifecycle.PER_CLASS)
    class WritePathTest {

        @Autowired AirQualityCollectService collectService;
        @Autowired AirQualityClient airQualityClient;

        private static final LocalDateTime T11 = LocalDateTime.of(2026, 5, 21, 11, 0);
        private static final LocalDateTime T10 = LocalDateTime.of(2026, 5, 21, 10, 0);

        private StubAirQualityClient stub() {
            return (StubAirQualityClient) airQualityClient;
        }

        @BeforeEach
        void cleanAndSeed() throws Exception {
            seedSido();
        }

        @Test
        @DisplayName("교집합만 batch INSERT 되어 DB 에 적재")
        void collectsIntersectionToDb() {
            Long sidoId = seoulSidoId();
            stub().set(
                    List.of(new AirQuality(sidoId, T11, 7, null),
                            new AirQuality(sidoId, T10, 6, null)),
                    List.of(new AirQuality(sidoId, T11, null, 4)));

            collectService.collect();

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM air_quality", Integer.class);
            assertThat(count).isEqualTo(1);

            Integer pm10 = jdbcTemplate.queryForObject(
                    "SELECT pm10 FROM air_quality WHERE announce_time = ?", Integer.class, T11);
            Integer pm25 = jdbcTemplate.queryForObject(
                    "SELECT pm25 FROM air_quality WHERE announce_time = ?", Integer.class, T11);
            assertThat(pm10).isEqualTo(7);
            assertThat(pm25).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("읽기 경로 -- forecast 조회 (findLatestBySido JPQL)")
    @TestInstance(Lifecycle.PER_CLASS)
    class ForecastReadPathTest {

        @Autowired AirQualityService airQualityService;
        @Autowired AirQualityCacheManager cacheManager;

        @BeforeEach
        void seed() throws Exception {
            seedSido();
            cacheManager.invalidate();
        }

        @Test
        @DisplayName("여러 측정 중 가장 최신 1건 -> view 매핑 + 등급 (시간 제한 없음)")
        void findsLatest() {
            Long sidoId = seoulSidoId();
            insertAirQuality(sidoId, FIXED_NOW.minusHours(2), 95, 80);
            insertAirQuality(sidoId, FIXED_NOW.minusHours(4), 10, 5);

            AirQualityView view = airQualityService.getAirQuality(SEOUL_REGION_ID);

            assertThat(view).isNotNull();
            assertThat(view.announceTime()).isEqualTo(FIXED_NOW.minusHours(2));
            assertThat(view.pm10()).isEqualTo(95);
            assertThat(view.pm10Grade()).isEqualTo("BAD");
            assertThat(view.pm25Grade()).isEqualTo("VERY_BAD");
        }

        @Test
        @DisplayName("오래된 측정만 존재해도 반환 (제한 없음, 신선도는 announceTime 으로 판단)")
        void returnsStaleMeasurement() {
            Long sidoId = seoulSidoId();
            // 3시간을 한참 넘긴 측정도 반환됨
            insertAirQuality(sidoId, FIXED_NOW.minusHours(10), 50, 30);

            AirQualityView view = airQualityService.getAirQuality(SEOUL_REGION_ID);

            assertThat(view).isNotNull();
            assertThat(view.announceTime()).isEqualTo(FIXED_NOW.minusHours(10));
            assertThat(view.pm10()).isEqualTo(50);
        }

        @Test
        @DisplayName("측정 자체가 없음 -> 빈 view")
        void noMeasurement_emptyView() {
            // air_quality 비어있음
            AirQualityView view = airQualityService.getAirQuality(SEOUL_REGION_ID);

            assertThat(view.announceTime()).isNull();
            assertThat(view.pm10()).isNull();
        }
    }

    @Nested
    @DisplayName("읽기 경로 -- notification 알림 생성")
    @TestInstance(Lifecycle.PER_CLASS)
    class NotificationReadPathTest {

        @Autowired GenerateAlertsService generateAlertsService;
        @Autowired
        com.github.yun531.climate.notification.infra.alert.AirQualityViewCacheManager cacheManager;

        @BeforeEach
        void seed() throws Exception {
            seedSido();
            cacheManager.invalidate();
        }

        @Test
        @DisplayName("임계 초과 측정 -> AIR_POLLUTION 알림 생성 (PM10+PM25)")
        void generatesAirPollutionAlert() {
            Long sidoId = seoulSidoId();
            insertAirQuality(sidoId, FIXED_NOW.minusHours(1), 95, 80);

            var cmd = new GenerateAlertsCommand(
                    List.of(SEOUL_REGION_ID),
                    EnumSet.of(AlertTypeEnum.AIR_POLLUTION),
                    null, null);

            List<AlertEvent> events = generateAlertsService.generate(cmd, FIXED_NOW);

            List<AlertEvent> airAlerts = events.stream()
                    .filter(e -> e.type() == AlertTypeEnum.AIR_POLLUTION)
                    .toList();

            assertThat(airAlerts).hasSize(2);
            assertThat(airAlerts).extracting(e -> ((AirPollutionPayload) e.payload()).pollutionType())
                    .containsExactlyInAnyOrder("PM10", "PM25");
            assertThat(airAlerts).extracting(e -> ((AirPollutionPayload) e.payload()).grade())
                    .containsExactlyInAnyOrder("BAD", "VERY_BAD");
        }

        @Test
        @DisplayName("임계 이하 측정 -> 알림 없음")
        void belowThreshold_noAlert() {
            Long sidoId = seoulSidoId();
            insertAirQuality(sidoId, FIXED_NOW.minusHours(1), 50, 20);

            var cmd = new GenerateAlertsCommand(
                    List.of(SEOUL_REGION_ID),
                    EnumSet.of(AlertTypeEnum.AIR_POLLUTION),
                    null, null);

            List<AlertEvent> events = generateAlertsService.generate(cmd, FIXED_NOW);

            assertThat(events).noneMatch(e -> e.type() == AlertTypeEnum.AIR_POLLUTION);
        }
    }
}