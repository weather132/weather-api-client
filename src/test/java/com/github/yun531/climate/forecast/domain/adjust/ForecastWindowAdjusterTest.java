package com.github.yun531.climate.forecast.domain.adjust;

import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyPoint;
import com.github.yun531.climate.forecast.domain.readmodel.ForecastHourlyView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForecastWindowAdjusterTest {

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 1, 22, 5, 0);

    private final ForecastWindowAdjuster adjuster = new ForecastWindowAdjuster(2, 24);


    @Nested
    @DisplayName("생성자 검증")
    class Constructor {

        @Test
        void negativeMaxShift_throws() {
            assertThatThrownBy(() -> new ForecastWindowAdjuster(-1, 24))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void zeroWindowSize_throws() {
            assertThatThrownBy(() -> new ForecastWindowAdjuster(2, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }


    @Nested
    @DisplayName("null 가드")
    class NullGuards {

        @Test
        @DisplayName("base가 null 이면 null 반환")
        void nullBase_returnsNull() {
            assertThat(adjuster.adjust(null, ANNOUNCE_TIME)).isNull();
        }

        @Test
        @DisplayName("announceTime이 null 이면 시프트 없이 정렬만")
        void nullAnnounceTime_noShift() {
            ForecastHourlyView base = new ForecastHourlyView("R1", null, List.of(
                    new ForecastHourlyPoint(ANNOUNCE_TIME.plusHours(1), 10, 20)
            ));

            ForecastHourlyView result = adjuster.adjust(base, ANNOUNCE_TIME);

            assertThat(result.announceTime()).isNull();
            assertThat(result.hourlyPoints()).hasSize(1);
        }
    }


    @Nested
    @DisplayName("시프트 및 클램프")
    class ShiftAndClip {

        @Test
        @DisplayName("now == announceTime -> 시프트 없이 announceTime 이후(초과) 포인트만 반환")
        void noShift_filtersStrictlyAfterAnnounceTime() {
            ForecastHourlyView base = new ForecastHourlyView("R1", ANNOUNCE_TIME, List.of(
                    new ForecastHourlyPoint(ANNOUNCE_TIME, 10, 20),
                    new ForecastHourlyPoint(ANNOUNCE_TIME.plusHours(1), 12, 30),
                    new ForecastHourlyPoint(ANNOUNCE_TIME.plusHours(2), 14, 40)
            ));

            ForecastHourlyView result = adjuster.adjust(base, ANNOUNCE_TIME);

            assertThat(result.hourlyPoints()).hasSize(2);
            result.hourlyPoints().forEach(p ->
                    assertThat(p.effectiveTime()).isAfter(ANNOUNCE_TIME));
        }

        @Test
        @DisplayName("now = announceTime + 1h -> 1시간 시프트, 시프트된 시각 이후만")
        void oneHourShift() {
            LocalDateTime now       = ANNOUNCE_TIME.plusHours(1);
            ForecastHourlyView base = buildView(ANNOUNCE_TIME, 26);

            ForecastHourlyView result = adjuster.adjust(base, now);

            assertThat(result.announceTime()).isEqualTo(ANNOUNCE_TIME.plusHours(1));
            result.hourlyPoints().forEach(p ->
                    assertThat(p.effectiveTime()).isAfter(ANNOUNCE_TIME.plusHours(1)));
        }

        @Test
        @DisplayName("now = announceTime + 5h -> maxShift(2)로 클램프")
        void exceedsMax_clampsShift() {
            LocalDateTime now = ANNOUNCE_TIME.plusHours(5);
            ForecastHourlyView base = buildView(ANNOUNCE_TIME, 26);

            ForecastHourlyView result = adjuster.adjust(base, now);

            assertThat(result.announceTime()).isEqualTo(ANNOUNCE_TIME.plusHours(2));
        }
    }


    @Nested
    @DisplayName("윈도우 필터링")
    class WindowFiltering {

        @Test
        @DisplayName("빈 hourlyPoints -> 빈 리스트 반환")
        void emptyPoints_returnsEmpty() {
            ForecastHourlyView base = new ForecastHourlyView("R1", ANNOUNCE_TIME, List.of());

            ForecastHourlyView result = adjuster.adjust(base, ANNOUNCE_TIME);

            assertThat(result.hourlyPoints()).isEmpty();
        }

        @Test
        @DisplayName("effectiveTime이 null인 포인트 -> 제외")
        void nullEffectiveTime_filtered() {
            ForecastHourlyView base = new ForecastHourlyView("R1", ANNOUNCE_TIME, List.of(
                    new ForecastHourlyPoint(null, 10, 20),
                    new ForecastHourlyPoint(ANNOUNCE_TIME.plusHours(1), 12, 30)
            ));

            ForecastHourlyView result = adjuster.adjust(base, ANNOUNCE_TIME);

            assertThat(result.hourlyPoints()).hasSize(1);
            assertThat(result.hourlyPoints().get(0).effectiveTime())
                    .isEqualTo(ANNOUNCE_TIME.plusHours(1));
        }

        @Test
        @DisplayName("windowSize=3 -> 최대 3개 포인트만 반환")
        void windowSizeLimit() {
            var smallAdjuster = new ForecastWindowAdjuster(2, 3);
            ForecastHourlyView base = buildView(ANNOUNCE_TIME, 26);

            ForecastHourlyView result = smallAdjuster.adjust(base, ANNOUNCE_TIME);

            assertThat(result.hourlyPoints()).hasSizeLessThanOrEqualTo(3);
        }
    }

    // ==================== helper ====================

    private ForecastHourlyView buildView(LocalDateTime announceTime, int count) {
        List<ForecastHourlyPoint> points = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            points.add(new ForecastHourlyPoint(
                    announceTime.plusHours(i + 1), i * 2, i * 3));
        }
        return new ForecastHourlyView("R1", announceTime, points);
    }
}