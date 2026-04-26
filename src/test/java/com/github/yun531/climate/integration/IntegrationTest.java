package com.github.yun531.climate.integration;

import com.github.yun531.climate.forecast.application.ForecastService;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastDailyPoint;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastDailyView;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyPoint;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyView;
import com.github.yun531.climate.notification.application.alert.GenerateAlertsCommand;
import com.github.yun531.climate.notification.application.alert.GenerateAlertsService;
import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload;
import com.github.yun531.climate.notification.domain.payload.RainOnsetPayload;
import com.github.yun531.climate.notification.domain.payload.WarningIssuedPayload;
import com.github.yun531.climate.notification.domain.readmodel.PopView;
import com.github.yun531.climate.notification.infra.alert.PopCacheManager;
import com.github.yun531.climate.warning.application.WarningCollectService;
import com.github.yun531.climate.warning.domain.WarningClient;
import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import com.github.yun531.climate.warning.domain.model.WarningEventType;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers
@Import(IntegrationTest.WarningClientStubConfig.class)
class IntegrationTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("climate")
                    .withUsername("test")
                    .withPassword("test")
                    .withCommand("--log-bin-trust-function-creators=1")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("schema.sql"),
                            "/schema.sql"
                    )
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("integration-init.sql"),
                            "/init-procedures.sql"
                    );

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
                "sh", "-c",
                "mysql -u test -ptest climate < /schema.sql && mysql -u test -ptest climate < /init-procedures.sql"
        );
        if (result.getExitCode() != 0) {
            throw new RuntimeException("init failed: " + result.getStderr());
        }
    }

    private static final String REGION_ID              = "11B10101";
    private static final String NON_EXISTENT_REGION_ID = "99Z99999";
    private static final int EXPECTED_FORECAST_DAYS    = 7;

    @Autowired ForecastService forecastService;
    @Autowired GenerateAlertsService generateAlertsService;
    @Autowired PopCacheManager popCacheManager;
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbcTemplate;

    // =====================================================================
    //  StubWarningClient: 기상청 API 대체 (WritePathTest 전용)
    // =====================================================================
    @TestConfiguration
    static class WarningClientStubConfig {

        @Bean
        @Primary
        public WarningClient warningClient() {
            return new StubWarningClient();
        }
    }

    static class StubWarningClient implements WarningClient {
        private List<WarningCurrent> response = List.of();

        void setResponse(List<WarningCurrent> response) {
            this.response = response;
        }

        @Override
        public List<WarningCurrent> requestCurrentWarnings(LocalDateTime tm) {
            return response;
        }
    }

    @Nested
    @DisplayName("쓰기 경로 통합 테스트")
    @TestInstance(Lifecycle.PER_CLASS)
    class WritePathTest {

        @Autowired WarningCollectService warningCollectService;
        @Autowired WarningClient warningClient;

        private static final LocalDateTime FIRST_ANNOUNCE_TIME = LocalDateTime.of(2026, 3, 30, 12, 0);
        private static final LocalDateTime FIRST_EFFECTIVE_TIME = LocalDateTime.of(2026, 3, 30, 14, 0);
        private static final LocalDateTime SECOND_ANNOUNCE_TIME = LocalDateTime.of(2026, 3, 30, 15, 0);
        private static final LocalDateTime SECOND_EFFECTIVE_TIME = LocalDateTime.of(2026, 3, 30, 18, 0);

        private StubWarningClient stub() {
            return (StubWarningClient) warningClient;
        }

        @BeforeEach
        void cleanWarningTables() throws Exception {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                stmt.execute("TRUNCATE TABLE warning_current");
                stmt.execute("TRUNCATE TABLE warning_event");
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        }

        @Nested
        @DisplayName("경로: Warning 수집")
        class WarningCollectPath {

            @Test
            @DisplayName("첫 수집: warning_current 저장 및 NEW 이벤트 생성")
            void firstCollectCreatesNewEvents() {
                stub().setResponse(List.of(
                        new WarningCurrent("L1100100", WarningKind.WIND, WarningLevel.ADVISORY,
                                FIRST_ANNOUNCE_TIME, FIRST_EFFECTIVE_TIME)
                ));

                warningCollectService.collect(FIRST_ANNOUNCE_TIME);

                Integer currentCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM warning_current", Integer.class);
                assertThat(currentCount)
                        .as("warning_current 건수")
                        .isEqualTo(1);

                List<Map<String, Object>> events = jdbcTemplate.queryForList(
                        "SELECT * FROM warning_event ORDER BY id");
                assertThat(events)
                        .as("warning_event 건수")
                        .hasSize(1);

                Map<String, Object> event = events.get(0);
                assertThat(event.get("warning_region_code")).isEqualTo("L1100100");
                assertThat(event.get("kind")).isEqualTo("WIND");
                assertThat(event.get("level")).isEqualTo("ADVISORY");
                assertThat(event.get("prev_level")).isNull();
                assertThat(event.get("event_type")).isEqualTo("NEW");
            }

            @Test
            @DisplayName("동일 데이터 재수집: 추가 이벤트 없음")
            void identicalCollectProducesNoNewEvents() {
                List<WarningCurrent> data = List.of(
                        new WarningCurrent("L1100100", WarningKind.WIND, WarningLevel.ADVISORY,
                                FIRST_ANNOUNCE_TIME, FIRST_EFFECTIVE_TIME)
                );
                stub().setResponse(data);

                warningCollectService.collect(FIRST_ANNOUNCE_TIME);
                warningCollectService.collect(FIRST_ANNOUNCE_TIME);

                Integer eventCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM warning_event", Integer.class);
                assertThat(eventCount)
                        .as("동일 데이터 재수집 후 이벤트 건수")
                        .isEqualTo(1);
            }

            @Test
            @DisplayName("변화 수집: UPGRADED 이벤트 생성")
            void changedCollectCreatesUpgradedEvent() {
                stub().setResponse(List.of(
                        new WarningCurrent("L1100100", WarningKind.WIND, WarningLevel.ADVISORY,
                                FIRST_ANNOUNCE_TIME, FIRST_EFFECTIVE_TIME)
                ));
                warningCollectService.collect(FIRST_ANNOUNCE_TIME);

                stub().setResponse(List.of(
                        new WarningCurrent("L1100100", WarningKind.WIND, WarningLevel.WARNING,
                                SECOND_ANNOUNCE_TIME, SECOND_EFFECTIVE_TIME)
                ));
                warningCollectService.collect(SECOND_ANNOUNCE_TIME);

                String currentLevel = jdbcTemplate.queryForObject(
                        "SELECT level FROM warning_current WHERE warning_region_code = 'L1100100'",
                        String.class);
                assertThat(currentLevel)
                        .as("warning_current level")
                        .isEqualTo("WARNING");

                List<Map<String, Object>> events = jdbcTemplate.queryForList(
                        "SELECT * FROM warning_event ORDER BY id");
                assertThat(events)
                        .as("warning_event 건수")
                        .hasSize(2);

                Map<String, Object> newEvent = events.get(0);
                assertThat(newEvent.get("event_type")).isEqualTo("NEW");
                assertThat(newEvent.get("level")).isEqualTo("ADVISORY");

                Map<String, Object> upgradedEvent = events.get(1);
                assertThat(upgradedEvent.get("event_type")).isEqualTo("UPGRADED");
                assertThat(upgradedEvent.get("level")).isEqualTo("WARNING");
                assertThat(upgradedEvent.get("prev_level")).isEqualTo("ADVISORY");
            }

            @Test
            @DisplayName("특보 해제: LIFTED 이벤트 생성")
            void liftedCollectCreatesLiftedEvent() {
                stub().setResponse(List.of(
                        new WarningCurrent("L1100100", WarningKind.WIND, WarningLevel.ADVISORY,
                                FIRST_ANNOUNCE_TIME, FIRST_EFFECTIVE_TIME)
                ));
                warningCollectService.collect(FIRST_ANNOUNCE_TIME);

                stub().setResponse(List.of());
                warningCollectService.collect(SECOND_ANNOUNCE_TIME);

                Integer currentCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM warning_current", Integer.class);
                assertThat(currentCount)
                        .as("해제 후 warning_current 건수")
                        .isEqualTo(0);

                List<Map<String, Object>> events = jdbcTemplate.queryForList(
                        "SELECT * FROM warning_event ORDER BY id");
                assertThat(events)
                        .as("warning_event 건수")
                        .hasSize(2);

                assertThat(events.get(0).get("event_type")).isEqualTo("NEW");
                assertThat(events.get(1).get("event_type")).isEqualTo("LIFTED");
            }
        }
    }


    @Nested
    @DisplayName("읽기 경로 통합 테스트")
    @TestInstance(Lifecycle.PER_CLASS)
    class ReadPathTest {

        private static final Integer[] LATEST_POPS = {
                0, 0, 30, 70, 80, 70, 30, 0, 0, 0,
                0, 0, 70, 80, 70, 0, 0, 60, 70, 80,
                60, 0, 0, 0, 0, 0
        };
        private static final Integer[] PAST_POPS = {
                0, 0, 0, 20, 70, 80, 70, 30, 0, 0,
                60, 70, 80, 70, 60, 0, 0, 0, 0, 30,
                50, 70, 80, 70, 30, 0
        };
        private static final int[] EXPECTED_ONSET_HOURS = {5, 6, 13, 14, 15, 18};


        @BeforeAll
        void setUp() throws Exception {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SET time_zone = '+09:00'");
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                stmt.execute("TRUNCATE TABLE short_grid");
                stmt.execute("TRUNCATE TABLE short_land");
                stmt.execute("TRUNCATE TABLE mid_pop");
                stmt.execute("TRUNCATE TABLE mid_temperature");
                stmt.execute("TRUNCATE TABLE warning_current");
                stmt.execute("TRUNCATE TABLE warning_event");
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                stmt.execute("CALL insert_all()");
            }

            warmPreviousPopCache();
        }

        /**
         * RAIN_ONSET 테스트를 위한 previous POP 캐시 pre-warm.
         * - 통합 테스트에서는 SQL로 데이터를 직접 삽입하므로 이벤트 기반 rotate가 발생하지 않아,
         *   past announceTime의 PAST_POPS 데이터로 PopView를 수동 구성하여 previous 캐시에 배치.
         */
        private void warmPreviousPopCache() {
            LocalDateTime pastAt = jdbcTemplate.queryForObject(
                    "SELECT MIN(announce_time) FROM short_grid", LocalDateTime.class);

            List<PopView.Hourly.Pop> hourlyPops = new ArrayList<>(PopView.HOURLY_SIZE);
            for (int i = 0; i < PAST_POPS.length; i++) {
                hourlyPops.add(new PopView.Hourly.Pop(pastAt.plusHours(i + 1), PAST_POPS[i]));
            }

            List<PopView.Daily.Pop> dailyPops = new ArrayList<>(PopView.DAILY_SIZE);
            for (int i = 0; i < PopView.DAILY_SIZE; i++) {
                dailyPops.add(new PopView.Daily.Pop(null, null));
            }

            PopView pastView = new PopView(
                    new PopView.Hourly(hourlyPops),
                    new PopView.Daily(dailyPops),
                    pastAt
            );

            // putCurrent → rotate → PAST_POPS가 previous로 이동
            popCacheManager.putCurrent(REGION_ID, pastView);
            popCacheManager.rotate();
        }

        /**
         * DB 에서 가장 최근 ShortGrid 발표시각을 조회.
         */
        private LocalDateTime loadAnnounceTime() {
            LocalDateTime announceTime = jdbcTemplate.queryForObject(
                    "SELECT MAX(announce_time) FROM short_grid", LocalDateTime.class);
            assertThat(announceTime).as("short_grid announceTime").isNotNull();
            return announceTime;
        }

        @Nested
        @DisplayName("경로: Forecast")
        class ForecastPath {

            @Test
            @DisplayName("hourlyForecast의 POP 값이 시나리오와 일치")
            void hourlyForecastPopMatchesScenario() {
                LocalDateTime announceTime = loadAnnounceTime();

                ForecastHourlyView hourly = forecastService.getHourlyForecast(REGION_ID);
                assertThat(hourly).as("시간별 예보 뷰").isNotNull();

                List<ForecastHourlyPoint> points = hourly.hourlyPoints();
                LocalDateTime shiftedAnnounceTime = hourly.announceTime();
                int shiftHours = (int) Duration.between(announceTime, shiftedAnnounceTime).toHours();

                assertThat(points)
                        .as("hourly 포인트는 항상 24개여야 한다 (windowSize=24)")
                        .hasSize(24);

                assertThat(points)
                        .allMatch(p -> p.effectiveTime().isAfter(shiftedAnnounceTime));

                for (ForecastHourlyPoint p : points) {
                    long offsetFromShifted = Duration.between(shiftedAnnounceTime, p.effectiveTime()).toHours();
                    int latestPopsIndex = (int) offsetFromShifted + shiftHours - 1;

                    assertThat(p.pop())
                            .as("forecast +%dh POP (shiftHours=%d, originalI=%d)",
                                    offsetFromShifted, shiftHours, latestPopsIndex + 1)
                            .isEqualTo(LATEST_POPS[latestPopsIndex]);
                }
            }

            @Test
            @DisplayName("dailyForecast 7일치 생성 및 daysAhead 시퀀스")
            void createsDailyForecastWithSevenDays() {
                ForecastDailyView daily = forecastService.getDailyForecast(REGION_ID);

                assertThat(daily).as("일별 예보 뷰").isNotNull();
                assertThat(daily.dailyPoints())
                        .as("일별 예보는 %d 일치여야 한다", EXPECTED_FORECAST_DAYS)
                        .hasSize(EXPECTED_FORECAST_DAYS);

                List<Integer> daysAhead = daily.dailyPoints().stream()
                        .map(ForecastDailyPoint::daysAhead)
                        .toList();

                assertThat(daysAhead)
                        .as("daysAhead 시퀀스는 0부터 연속이어야 한다")
                        .containsExactly(0, 1, 2, 3, 4, 5, 6);
            }

            @Test
            @DisplayName("미존재 regionId -> null 반환")
            void returnsNullForUnknownRegion() {
                assertThat(forecastService.getHourlyForecast(NON_EXISTENT_REGION_ID))
                        .as("미존재 regionId hourly")
                        .isNull();
                assertThat(forecastService.getDailyForecast(NON_EXISTENT_REGION_ID))
                        .as("미존재 regionId daily")
                        .isNull();
            }
        }

        @Nested
        @DisplayName("경로: Notification")
        class NotificationPath {

            @Test
            @DisplayName("RAIN_ONSET: 정확히 6건, 각 effectiveTime이 시나리오와 일치")
            void detectsRainOnsetWithExactTimesAndCount() {
                LocalDateTime announceTime = loadAnnounceTime();
                var cmd = new GenerateAlertsCommand(
                        List.of(REGION_ID),
                        EnumSet.of(AlertTypeEnum.RAIN_ONSET),
                        null,
                        null
                );
                List<AlertEvent> events = generateAlertsService.generate(cmd, announceTime);

                List<AlertEvent> onsetEvents = events.stream()
                        .filter(e -> e.type() == AlertTypeEnum.RAIN_ONSET)
                        .toList();

                assertThat(onsetEvents)
                        .as("RAIN_ONSET 이벤트 건수")
                        .hasSize(EXPECTED_ONSET_HOURS.length);

                List<LocalDateTime> expectedTimes = IntStream.of(EXPECTED_ONSET_HOURS)
                        .mapToObj(announceTime::plusHours)
                        .toList();

                List<LocalDateTime> actualTimes = onsetEvents.stream()
                        .map(e -> ((RainOnsetPayload) e.payload()).effectiveTime())
                        .toList();

                assertThat(actualTimes)
                        .as("onset effectiveTime 목록")
                        .containsExactlyInAnyOrderElementsOf(expectedTimes);
            }

            @Test
            @DisplayName("RAIN_ONSET: 각 이벤트의 POP 값이 시나리오와 일치")
            void rainOnsetPopValuesMatchScenario() {
                LocalDateTime announceTime = loadAnnounceTime();
                var cmd = new GenerateAlertsCommand(
                        List.of(REGION_ID),
                        EnumSet.of(AlertTypeEnum.RAIN_ONSET),
                        null,
                        null
                );
                List<AlertEvent> events = generateAlertsService.generate(cmd, announceTime);

                Map<LocalDateTime, Integer> popByTime = events.stream()
                        .filter(e -> e.type() == AlertTypeEnum.RAIN_ONSET)
                        .map(e -> (RainOnsetPayload) e.payload())
                        .collect(Collectors.toMap(
                                RainOnsetPayload::effectiveTime,
                                RainOnsetPayload::pop));

                for (int hour : EXPECTED_ONSET_HOURS) {
                    LocalDateTime et = announceTime.plusHours(hour);
                    int expectedPop = LATEST_POPS[hour - 1];  // LATEST_POPS는 0-based 이므로 -1
                    assertThat(popByTime.get(et))
                            .as("onset +%dh POP", hour)
                            .isEqualTo(expectedPop);
                }
            }

            @Test
            @DisplayName("RAIN_FORECAST: 3개 비 구간 생성, 시작/종료 시각 검증")
            void rainForecastProducesThreeRainIntervals() {
                LocalDateTime announceTime = loadAnnounceTime();
                var cmd = new GenerateAlertsCommand(
                        List.of(REGION_ID),
                        EnumSet.of(AlertTypeEnum.RAIN_FORECAST),
                        null,
                        null
                );
                List<AlertEvent> events = generateAlertsService.generate(cmd, announceTime);

                assertThat(events)
                        .as("RAIN_FORECAST 이벤트가 1건 생성되어야 한다")
                        .hasSize(1);

                AlertEvent forecastEvent = events.get(0);
                assertThat(forecastEvent.type()).isEqualTo(AlertTypeEnum.RAIN_FORECAST);

                RainForecastPayload payload = (RainForecastPayload) forecastEvent.payload();
                List<RainForecastPayload.RainInterval> intervals = payload.hourlyParts();

                assertThat(intervals)
                        .as("비 구간 개수 (i=4~6, 13~15, 18~21)")
                        .hasSize(3);

                // 구간 1: latest_at+4h ~ latest_at+6h
                assertThat(intervals.get(0).start())
                        .as("구간1 시작")
                        .isEqualTo(announceTime.plusHours(4));
                assertThat(intervals.get(0).end())
                        .as("구간1 종료")
                        .isEqualTo(announceTime.plusHours(6));

                // 구간 2: latest_at+13h ~ latest_at+15h
                assertThat(intervals.get(1).start())
                        .as("구간2 시작")
                        .isEqualTo(announceTime.plusHours(13));
                assertThat(intervals.get(1).end())
                        .as("구간2 종료")
                        .isEqualTo(announceTime.plusHours(15));

                // 구간 3: latest_at+18h ~ latest_at+21h
                assertThat(intervals.get(2).start())
                        .as("구간3 시작")
                        .isEqualTo(announceTime.plusHours(18));
                assertThat(intervals.get(2).end())
                        .as("구간3 종료")
                        .isEqualTo(announceTime.plusHours(21));
            }

            @Test
            @DisplayName("RAIN_FORECAST: dayParts 7일치 생성")
            void rainForecastDayPartsHaveSevenEntries() {
                LocalDateTime announceTime = loadAnnounceTime();
                var cmd = new GenerateAlertsCommand(
                        List.of(REGION_ID),
                        EnumSet.of(AlertTypeEnum.RAIN_FORECAST),
                        null,
                        null
                );
                List<AlertEvent> events = generateAlertsService.generate(cmd, announceTime);

                RainForecastPayload payload = (RainForecastPayload) events.get(0).payload();

                assertThat(payload.dayParts())
                        .as("dayParts 7일치")
                        .hasSize(EXPECTED_FORECAST_DAYS);
            }

            @Test
            @DisplayName("WARNING_ISSUED: active 이벤트만 AlertEvent로 변환")
            void detectsWarningIssuedForActiveEvents() {
                var cmd = new GenerateAlertsCommand(
                        List.of(REGION_ID),
                        EnumSet.of(AlertTypeEnum.WARNING_ISSUED),
                        null,
                        null
                );

                List<AlertEvent> events = generateAlertsService.generate(cmd);

                List<AlertEvent> warningEvents = events.stream()
                        .filter(e -> e.type() == AlertTypeEnum.WARNING_ISSUED)
                        .toList();

                assertThat(warningEvents)
                        .as("WARNING_ISSUED 이벤트 건수")
                        .hasSize(1);

                WarningIssuedPayload payload = (WarningIssuedPayload) warningEvents.get(0).payload();
                assertThat(payload.kind()).isEqualTo("RAIN");
                assertThat(payload.level()).isEqualTo("WARNING");
                assertThat(payload.prevLevel()).isEqualTo("ADVISORY");
                assertThat(payload.eventType()).isEqualTo("UPGRADED");
            }

            @Test
            @DisplayName("WARNING_ISSUED: warningKinds 필터로 HEAT만 요청 시 빈 결과")
            void warningIssuedRespectsKindFilter() {
                var cmd = new GenerateAlertsCommand(
                        List.of(REGION_ID),
                        EnumSet.of(AlertTypeEnum.WARNING_ISSUED),
                        Set.of("HEAT"),
                        null
                );

                List<AlertEvent> events = generateAlertsService.generate(cmd);

                assertThat(events)
                        .as("HEAT 필터 시 WARNING_ISSUED 이벤트")
                        .isEmpty();
            }

            @Test
            @DisplayName("미존재 regionId -> 빈 리스트")
            void returnsEmptyForUnknownRegion() {
                var cmd = new GenerateAlertsCommand(
                        List.of(NON_EXISTENT_REGION_ID),
                        EnumSet.of(AlertTypeEnum.RAIN_ONSET, AlertTypeEnum.RAIN_FORECAST, AlertTypeEnum.WARNING_ISSUED),
                        null,
                        null
                );

                List<AlertEvent> events = generateAlertsService.generate(cmd);

                assertThat(events)
                        .as("미존재 regionId에 대한 알림 이벤트")
                        .isEmpty();
            }
        }
    }
}