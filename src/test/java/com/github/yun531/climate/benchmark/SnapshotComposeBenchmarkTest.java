package com.github.yun531.climate.benchmark;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.snapshot.contract.WeatherSnapshot;
import com.github.yun531.climate.snapshot.domain.compose.SnapshotComposeService;
import com.github.yun531.climate.warning.domain.WarningClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers
@Tag("benchmark")
@TestMethodOrder(OrderAnnotation.class)
@Import(SnapshotComposeBenchmarkTest.StubConfig.class)
class SnapshotComposeBenchmarkTest {

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

    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        public WarningClient warningClient() {
            return tm -> List.of();
        }
    }

    @Autowired SnapshotComposeService composeService;
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbcTemplate;

    private static List<CityRegionCode> allRegions;
    private static LocalDateTime announceTime;

    @BeforeAll
    static void setUp(
            @Autowired DataSource dataSource,
            @Autowired CityRegionCodeRepository cityRegionCodeRepository,
            @Autowired JdbcTemplate jdbcTemplate
    ) throws Exception {
        var result = mysql.execInContainer(
                "sh", "-c",
                "mysql -u test -ptest climate < /schema.sql "
                        + "&& mysql -u test -ptest climate < /init-procedures.sql"
        );
        if (result.getExitCode() != 0) {
            throw new RuntimeException("init failed: " + result.getStderr());
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET time_zone = '+09:00'");
            stmt.execute("CALL insert_all()");
        }

        allRegions = cityRegionCodeRepository.findAll();
        assertThat(allRegions).isNotEmpty();

        announceTime = jdbcTemplate.queryForObject(
                "SELECT announce_time FROM short_grid ORDER BY announce_time DESC LIMIT 1",
                LocalDateTime.class
        );
        assertThat(announceTime).isNotNull();

        System.out.printf("[SETUP] 전체 지역 수: %d, announceTime: %s%n",
                allRegions.size(), announceTime);
    }

    @Test
    @Order(1)
    @DisplayName("검증: 첫 번째 지역 compose 성공 확인")
    void verifyFirstRegionCompose() {
        WeatherSnapshot snapshot = composeService.composeSnapshot(
                allRegions.get(0).getRegionCode(), announceTime);

        assertThat(snapshot)
                .as("벤치마크 데이터 정합성 확인")
                .isNotNull();
        assertThat(snapshot.hourly()).isNotEmpty();
        assertThat(snapshot.daily()).isNotEmpty();

        System.out.printf("[VERIFY] regionId=%s, hourly=%d건, daily=%d건%n",
                snapshot.regionId(), snapshot.hourly().size(), snapshot.daily().size());
    }

    @Test
    @Order(2)
    @DisplayName("벤치마크: 전체 지역 스냅샷 compose 소요 시간 (best case)")
    void benchmarkAllRegionsCompose() {
        // ── 워밍업: 전체 1회 순회 ──
        for (CityRegionCode region : allRegions) {
            composeService.composeSnapshot(region.getRegionCode(), announceTime);
        }

        // ── 본 측정 ──
        int successCount = 0;
        int failCount = 0;
        long[] perRegionNanos = new long[allRegions.size()];

        long totalStart = System.nanoTime();

        for (int i = 0; i < allRegions.size(); i++) {
            String regionId = allRegions.get(i).getRegionCode();

            long regionStart = System.nanoTime();
            WeatherSnapshot snap = composeService.composeSnapshot(regionId, announceTime);
            perRegionNanos[i] = System.nanoTime() - regionStart;

            if (snap != null) successCount++;
            else failCount++;
        }

        long totalElapsed = System.nanoTime() - totalStart;

        // ── 통계 ──
        long totalMs = totalElapsed / 1_000_000;
        double avgMs = (double) totalElapsed / 1_000_000 / allRegions.size();

        record RegionTime(String regionId, long nanos) {}

        List<RegionTime> slowest = IntStream.range(0, allRegions.size())
                .mapToObj(i -> new RegionTime(
                        allRegions.get(i).getRegionCode(), perRegionNanos[i]))
                .sorted((a, b) -> Long.compare(b.nanos, a.nanos))
                .limit(5)
                .toList();

        // ── 결과 출력 ──
        System.out.printf("""
                
                ══════════════════════════════════════════════
                  BEST CASE BENCHMARK (ShortLand 존재)
                ══════════════════════════════════════════════
                  전체 지역 수 : %d
                  성공         : %d
                  실패 (null)  : %d
                  총 소요 시간 : %,d ms
                  지역당 평균  : %.2f ms
                ──────────────────────────────────────────────
                  상위 5 느린 지역:
                %s
                ══════════════════════════════════════════════
                """,
                allRegions.size(),
                successCount,
                failCount,
                totalMs,
                avgMs,
                slowest.stream()
                        .map(r -> String.format("    %s : %.2f ms",
                                r.regionId, r.nanos / 1_000_000.0))
                        .collect(Collectors.joining("\n"))
        );

        assertThat(successCount)
                .as("compose 성공 건수가 0이면 데이터 정합성 문제")
                .isGreaterThan(0);
    }

    @Test
    @Order(3)
    @DisplayName("벤치마크: worst case — ShortLand 전체 null, Mid fallback 강제")
    void benchmarkWorstCase_midFallbackOnly() throws Exception {
        try {
            // ── short_land 비우기 + D+0~D+2 mid 데이터 보충 ──
            jdbcTemplate.execute("TRUNCATE TABLE short_land");
            fillMissingMidData();

            // ── 워밍업 ──
            for (CityRegionCode region : allRegions) {
                composeService.composeSnapshot(region.getRegionCode(), announceTime);
            }

            // ── 본 측정 ──
            int successCount = 0;
            int failCount = 0;
            long[] perRegionNanos = new long[allRegions.size()];

            long totalStart = System.nanoTime();

            for (int i = 0; i < allRegions.size(); i++) {
                String regionId = allRegions.get(i).getRegionCode();

                long regionStart = System.nanoTime();
                WeatherSnapshot snap = composeService.composeSnapshot(regionId, announceTime);
                perRegionNanos[i] = System.nanoTime() - regionStart;

                if (snap != null) successCount++;
                else failCount++;
            }

            long totalElapsed = System.nanoTime() - totalStart;

            // ── 통계 ──
            long totalMs = totalElapsed / 1_000_000;
            double avgMs = (double) totalElapsed / 1_000_000 / allRegions.size();

            record RegionTime(String regionId, long nanos) {}

            List<RegionTime> slowest = IntStream.range(0, allRegions.size())
                    .mapToObj(i -> new RegionTime(
                            allRegions.get(i).getRegionCode(), perRegionNanos[i]))
                    .sorted((a, b) -> Long.compare(b.nanos, a.nanos))
                    .limit(5)
                    .toList();

            System.out.printf("""
                    
                    ══════════════════════════════════════════════
                      WORST CASE BENCHMARK (Mid fallback 강제)
                    ══════════════════════════════════════════════
                      전체 지역 수 : %d
                      성공         : %d
                      실패 (null)  : %d
                      총 소요 시간 : %,d ms
                      지역당 평균  : %.2f ms
                    ──────────────────────────────────────────────
                      상위 5 느린 지역:
                    %s
                    ══════════════════════════════════════════════
                    """,
                    allRegions.size(),
                    successCount,
                    failCount,
                    totalMs,
                    avgMs,
                    slowest.stream()
                            .map(r -> String.format("    %s : %.2f ms",
                                    r.regionId, r.nanos / 1_000_000.0))
                            .collect(Collectors.joining("\n"))
            );

            assertThat(successCount).isGreaterThan(0);

        } finally {
            // ── short_land 복원 (테스트 성공/실패 무관하게 실행) ──
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SET time_zone = '+09:00'");
                stmt.execute("CALL insert_short_land(DATE_SUB(NOW(), INTERVAL 10 MINUTE))");
            }
        }
    }

    // ==================== helper ====================

    /**
     * D+0 ~ D+2 구간의 mid_pop, mid_temperature 데이터를 보충한다.
     * 저장 프로시저는 D+3부터만 넣기 때문에, ShortLand를 비우면
     * D+0 ~ D+2가 Mid fallback을 타면서 null 데이터로 NPE 가 발생한다.
     */
    private void fillMissingMidData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseDate = now.getHour() < 6 ? now.minusDays(1) : now;
        LocalDateTime standardTime = baseDate
                .withHour(9).withMinute(0).withSecond(0).withNano(0);

        // get_mid_announceTime 과 동일한 로직
        int hour = now.getHour();
        LocalDateTime midAnnounceTime;
        if (hour >= 18) {
            midAnnounceTime = now.withHour(18).withMinute(0).withSecond(0).withNano(0);
        } else if (hour < 6) {
            midAnnounceTime = now.minusDays(1)
                    .withHour(18).withMinute(0).withSecond(0).withNano(0);
        } else {
            midAnnounceTime = now.withHour(6).withMinute(0).withSecond(0).withNano(0);
        }

        for (int day = 0; day < 3; day++) {
            LocalDateTime morning = standardTime.plusDays(day).withHour(9);
            LocalDateTime afternoon = standardTime.plusDays(day).withHour(21);

            insertMidPop(midAnnounceTime, morning);
            insertMidPop(midAnnounceTime, afternoon);
            insertMidTemp(midAnnounceTime, morning);
        }
    }

    private void insertMidPop(LocalDateTime announceTime, LocalDateTime effectiveTime) {
        jdbcTemplate.update("""
                INSERT IGNORE INTO mid_pop
                    (id, announce_time, effective_time, province_region_code_id, pop)
                SELECT null, ?, ?, p.id, 50
                FROM province_region_code p
                """, announceTime, effectiveTime);
    }

    private void insertMidTemp(LocalDateTime announceTime, LocalDateTime effectiveTime) {
        jdbcTemplate.update("""
                INSERT IGNORE INTO mid_temperature
                    (id, announce_time, effective_time, city_region_code_id, max_temp, min_temp)
                SELECT null, ?, ?, c.id, 20, 10
                FROM city_region_code c
                """, announceTime, effectiveTime);
    }
}