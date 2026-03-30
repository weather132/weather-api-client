package com.github.yun531.climate.integration;

import com.github.yun531.climate.forecast.application.ForecastService;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastDailyPoint;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastDailyView;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyView;
import com.github.yun531.climate.notification.application.alert.GenerateAlertsCommand;
import com.github.yun531.climate.notification.application.alert.GenerateAlertsService;
import com.github.yun531.climate.notification.domain.model.AlertEvent;
import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import com.github.yun531.climate.notification.domain.payload.RainForecastPayload;
import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.snapshot.contract.SnapshotReader;
import com.github.yun531.climate.snapshot.contract.WeatherSnapshot;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class IntegrationTest {

    private static final String REGION_ID              = "11B10101";
    private static final String NON_EXISTENT_REGION_ID = "99Z99999";
    private static final long ANNOUNCE_INTERVAL_HOURS  = 3;
    private static final int EXPECTED_FORECAST_DAYS    = 7;

    @Autowired SnapshotReader snapshotReader;
    @Autowired ForecastService forecastService;
    @Autowired GenerateAlertsService generateAlertsService;
    @Autowired DataSource dataSource;

    @Nested
    @DisplayName("읽기 경로 통합 테스트")
    @TestInstance(Lifecycle.PER_CLASS)
    class ReadPathTest {

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
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                stmt.execute("CALL insert_all()");
            }
        }

        @Nested
        @DisplayName("경로: Snapshot")
        class SnapshotPath {

            @Test
            @DisplayName("snapshot 정상 로드 및 기본 구조")
            void loadsSnapshotWithValidStructure() {
                WeatherSnapshot snapshot = snapshotReader.loadCurrent(REGION_ID);

                assertThat(snapshot)
                        .as("유효한 regionId로 로드한 snapshot은 null이 아니어야 한다")
                        .isNotNull();
                assertThat(snapshot.hourly())
                        .as("시간별 데이터가 비어 있지 않아야 한다")
                        .isNotEmpty();
                assertThat(snapshot.daily())
                        .as("일별 데이터가 비어 있지 않아야 한다")
                        .isNotEmpty();
            }

            @Test
            @DisplayName("current/previous 발표시각 3시간 차이")
            void announceTimeGapMatchesInterval() {
                WeatherSnapshot current = snapshotReader.loadCurrent(REGION_ID);
                WeatherSnapshot previous = snapshotReader.loadPrevious(REGION_ID);

                assertThat(current)
                        .as("current snapshot")
                        .isNotNull();
                assertThat(previous)
                        .as("previous snapshot")
                        .isNotNull();

                long actualGap = Duration.between(
                        previous.announceTime(), current.announceTime()
                ).toHours();

                assertThat(actualGap)
                        .as("current와 previous의 발표시각 차이(시간)")
                        .isEqualTo(ANNOUNCE_INTERVAL_HOURS);
            }

            @Test
            @DisplayName("미존재 regionId -> null 반환")
            void returnsNullForUnknownRegion() {
                assertThat(snapshotReader.loadCurrent(NON_EXISTENT_REGION_ID))
                        .as("존재하지 않는 regionId는 null을 반환해야 한다")
                        .isNull();
            }
        }

        @Nested
        @DisplayName("경로: Forecast")
        class ForecastPath {

            @Test
            @DisplayName("hourlyForecast 정상 생성")
            void createsHourlyForecast() {
                ForecastHourlyView hourly = forecastService.getHourlyForecast(REGION_ID);

                assertThat(hourly)
                        .as("시간별 예보 뷰")
                        .isNotNull();
                assertThat(hourly.hourlyPoints())
                        .as("시간별 예보 포인트 목록")
                        .isNotEmpty()
                        .allMatch(p -> p.effectiveTime().isAfter(hourly.announceTime()));
            }

            @Test
            @DisplayName("dailyForecast 정상 생성")
            void createsDailyForecastWithSevenDays() {
                ForecastDailyView daily = forecastService.getDailyForecast(REGION_ID);

                assertThat(daily)
                        .as("일별 예보 뷰")
                        .isNotNull();
                assertThat(daily.dailyPoints())
                        .as("일별 예보는 %d일치여야 한다", EXPECTED_FORECAST_DAYS)
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
            @DisplayName("RAIN_FORECAST 이벤트 생성")
            void generatesRainForecastEvent() {
                var cmd = alertCommandFor(REGION_ID, AlertTypeEnum.RAIN_FORECAST);

                List<AlertEvent> events = generateAlertsService.generate(cmd);

                assertThat(events)
                        .as("RAIN_FORECAST 이벤트가 최소 1건 생성되어야 한다")
                        .isNotEmpty();

                AlertEvent forecastEvent = events.stream()
                        .filter(e -> e.type() == AlertTypeEnum.RAIN_FORECAST)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError(
                                "RAIN_FORECAST 타입 이벤트가 존재해야 한다"));

                RainForecastPayload payload = (RainForecastPayload) forecastEvent.payload();

                assertThat(payload.hourlyParts())
                        .as("RainForecastPayload의 hourlyParts")
                        .isNotEmpty();
            }

            @Test
            @DisplayName("RAIN_ONSET 감지")
            void detectsRainOnset() {
                var cmd = alertCommandFor(REGION_ID, AlertTypeEnum.RAIN_ONSET);

                List<AlertEvent> events = generateAlertsService.generate(cmd);

                assertThat(events)
                        .as("RAIN_ONSET 이벤트가 최소 1건 감지되어야 한다")
                        .isNotEmpty();
                assertThat(events)
                        .as("모든 이벤트 타입이 RAIN_ONSET이어야 한다")
                        .allMatch(e -> e.type() == AlertTypeEnum.RAIN_ONSET);
            }

            @Test
            @DisplayName("미존재 regionId -> 빈 리스트")
            void returnsEmptyForUnknownRegion() {
                var cmd = alertCommandFor(
                        NON_EXISTENT_REGION_ID,
                        AlertTypeEnum.RAIN_ONSET, AlertTypeEnum.RAIN_FORECAST
                );

                List<AlertEvent> events = generateAlertsService.generate(cmd);

                assertThat(events)
                        .as("미존재 regionId에 대한 알림 이벤트")
                        .isEmpty();
            }
        }
    }

    // ==================== helper ====================

    private GenerateAlertsCommand alertCommandFor(
            String regionId, AlertTypeEnum... types) {
        return new GenerateAlertsCommand(
                List.of(regionId), null,
                EnumSet.copyOf(List.of(types)), null
        );
    }
}