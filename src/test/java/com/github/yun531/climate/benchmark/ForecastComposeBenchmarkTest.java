package com.github.yun531.climate.benchmark;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.forecast.domain.compose.DailyForecastComposer;
import com.github.yun531.climate.forecast.domain.compose.DailyForecastComposer.DailyComposeResult;
import com.github.yun531.climate.forecast.domain.compose.HourlyForecastComposer;
import com.github.yun531.climate.forecast.domain.compose.HourlyForecastComposer.HourlyComposeResult;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers
@Tag("benchmark")
@TestMethodOrder(OrderAnnotation.class)
@Import(ForecastComposeBenchmarkTest.StubConfig.class)
class ForecastComposeBenchmarkTest {

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

    @Autowired HourlyForecastComposer hourlyComposer;
    @Autowired DailyForecastComposer dailyComposer;
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbcTemplate;

    private static List<CityRegionCode> allRegions;

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

        System.out.printf("[SETUP] 전체 지역 수: %d%n", allRegions.size());
    }

    @Test
    @Order(1)
    @DisplayName("검증: 첫 번째 지역 compose 성공 확인")
    void verifyFirstRegionCompose() {
        CityRegionCode first = allRegions.get(0);

        HourlyComposeResult hourly = hourlyComposer.compose(first);
        DailyComposeResult daily = dailyComposer.compose(first);

        assertThat(hourly.forecastHourlyPoints())
                .as("hourly 벤치마크 데이터 정합성")
                .isNotEmpty();
        assertThat(daily.forecastDailyPoints())
                .as("daily 벤치마크 데이터 정합성")
                .isNotEmpty();

        System.out.printf("[VERIFY] regionId=%s, hourly=%d건, daily=%d건%n",
                first.getRegionCode(),
                hourly.forecastHourlyPoints().size(),
                daily.forecastDailyPoints().size());
    }

    @Test
    @Order(2)
    @DisplayName("벤치마크: 전체 지역 forecast compose 소요 시간 (best case)")
    void benchmarkAllRegionsCompose() {
        // ── 워밍업: 전체 1회 순회 ──
        for (CityRegionCode region : allRegions) {
            hourlyComposer.compose(region);
            dailyComposer.compose(region);
        }

        // ── 본 측정 ──
        int successCount = 0;
        int failCount = 0;
        long[] perRegionNanos = new long[allRegions.size()];

        long totalStart = System.nanoTime();

        for (int i = 0; i < allRegions.size(); i++) {
            CityRegionCode region = allRegions.get(i);

            long regionStart = System.nanoTime();
            HourlyComposeResult hourly = hourlyComposer.compose(region);
            DailyComposeResult daily = dailyComposer.compose(region);
            perRegionNanos[i] = System.nanoTime() - regionStart;

            if (!hourly.forecastHourlyPoints().isEmpty()
                    && !daily.forecastDailyPoints().isEmpty()) {
                successCount++;
            } else {
                failCount++;
            }
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
                  BEST CASE BENCHMARK (Forecast Composer)
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
            // ── short_land 비우기 ──
            jdbcTemplate.execute("TRUNCATE TABLE short_land");

            // ── 워밍업 ──
            for (CityRegionCode region : allRegions) {
                dailyComposer.compose(region);
            }

            // ── 본 측정 (daily만 — hourly는 ShortGrid 기반이라 영향 없음) ──
            int successCount = 0;
            int failCount = 0;
            long[] perRegionNanos = new long[allRegions.size()];

            long totalStart = System.nanoTime();

            for (int i = 0; i < allRegions.size(); i++) {
                CityRegionCode region = allRegions.get(i);

                long regionStart = System.nanoTime();
                DailyComposeResult daily = dailyComposer.compose(region);
                perRegionNanos[i] = System.nanoTime() - regionStart;

                if (!daily.forecastDailyPoints().isEmpty()) successCount++;
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
                      WORST CASE BENCHMARK (Mid Fallback Only)
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
                    .as("worst-case compose 성공 건수")
                    .isGreaterThan(0);

        } finally {
            // ── short_land 복원 ──
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SET time_zone = '+09:00'");
                stmt.execute("CALL insert_short_land(NOW() - INTERVAL 10 MINUTE)");
            }
        }
    }
}