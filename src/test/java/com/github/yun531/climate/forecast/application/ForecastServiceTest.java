package com.github.yun531.climate.forecast.application;

import com.github.yun531.climate.forecast.domain.adjust.FcstWindowAdjuster;
import com.github.yun531.climate.forecast.domain.reader.FcstViewReader;
import com.github.yun531.climate.forecast.domain.readmodel.FcstDailyPoint;
import com.github.yun531.climate.forecast.domain.readmodel.FcstDailyView;
import com.github.yun531.climate.forecast.domain.readmodel.FcstHourlyPoint;
import com.github.yun531.climate.forecast.domain.readmodel.FcstHourlyView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    FcstViewReader viewReader;
    @Mock
    FcstWindowAdjuster windowAdjuster;

    private ForecastService service;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 22, 5, 15);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            NOW.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @BeforeEach
    void setUp() {
        service = new ForecastService(viewReader, windowAdjuster, FIXED_CLOCK);
    }

    @Nested
    @DisplayName("getHourlyForecast")
    class GetHourlyForecast {

        @Test
        @DisplayName("reader 로드 -> adjuster 적용 결과 반환")
        void delegatesToReaderThenAdjuster() {
            FcstHourlyView base = new FcstHourlyView("R1", NOW, List.of(
                    new FcstHourlyPoint(NOW.plusHours(1), 10, 20),
                    new FcstHourlyPoint(NOW.plusHours(2), 12, 30)
            ));
            FcstHourlyView adjusted = new FcstHourlyView("R1", NOW, List.of(
                    new FcstHourlyPoint(NOW.plusHours(2), 12, 30)
            ));

            when(viewReader.loadHourly("R1")).thenReturn(base);
            when(windowAdjuster.adjust(eq(base), eq(NOW))).thenReturn(adjusted);

            assertThat(service.getHourlyForecast("R1")).isSameAs(adjusted);
        }

        @Test
        @DisplayName("reader null -> adjuster 호출 없이 null 반환")
        void readerReturnsNull() {
            when(viewReader.loadHourly("R1")).thenReturn(null);

            assertThat(service.getHourlyForecast("R1")).isNull();
            verify(windowAdjuster, never()).adjust(any(), any());
        }
    }

    @Nested
    @DisplayName("getDailyForecast")
    class GetDailyForecast {

        @Test
        @DisplayName("reader 결과 그대로 반환")
        void delegatesToReader() {
            FcstDailyView view = new FcstDailyView("R1", NOW, List.of(
                    new FcstDailyPoint(0, -5, 5, 30, 60)
            ));
            when(viewReader.loadDaily("R1")).thenReturn(view);

            assertThat(service.getDailyForecast("R1")).isSameAs(view);
        }

        @Test
        @DisplayName("reader null -> null 반환")
        void readerReturnsNull() {
            when(viewReader.loadDaily("R1")).thenReturn(null);

            assertThat(service.getDailyForecast("R1")).isNull();
        }
    }
}