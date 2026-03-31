package com.github.yun531.climate.warning.application;

import com.github.yun531.climate.warning.contract.IssuedWarning;
import com.github.yun531.climate.warning.domain.model.WarningEvent;
import com.github.yun531.climate.warning.domain.model.WarningEventType;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;
import com.github.yun531.climate.warning.domain.repository.RegionCodeMappingRepository;
import com.github.yun531.climate.warning.domain.repository.WarningEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IssuedWarningLoadServiceTest {

    private RegionCodeMappingRepository mappingRepository;
    private WarningEventRepository eventRepository;
    private MutableClock clock;
    private IssuedWarningLoadService service;

    private static final ZoneId SEOUL         = ZoneId.of("Asia/Seoul");
    private static final Instant BASE_INSTANT = LocalDateTime.of(2026, 3, 30, 12, 0)
            .atZone(SEOUL).toInstant();

    @BeforeEach
    void setUp() {
        mappingRepository = mock(RegionCodeMappingRepository.class);
        eventRepository = mock(WarningEventRepository.class);
        clock = new MutableClock(BASE_INSTANT, SEOUL);
        service = new IssuedWarningLoadService(mappingRepository, eventRepository, clock, 50);
    }

    @Nested
    @DisplayName("조기 반환")
    class EarlyReturn {

        @Test
        @DisplayName("null regionId 시 빈 리스트 반환")
        void nullRegionId() {
            List<IssuedWarning> result = service.loadIssuedWarnings(null);

            assertThat(result).isEmpty();
            verify(mappingRepository, never()).findWarningRegionCodes(anyString());
        }

        @Test
        @DisplayName("blank regionId 시 빈 리스트 반환")
        void blankRegionId() {
            List<IssuedWarning> result = service.loadIssuedWarnings("  ");

            assertThat(result).isEmpty();
            verify(mappingRepository, never()).findWarningRegionCodes(anyString());
        }

        @Test
        @DisplayName("매핑 없으면 이벤트 조회 안 함")
        void noMapping() {
            when(mappingRepository.findWarningRegionCodes("11B10101")).thenReturn(List.of());

            List<IssuedWarning> result = service.loadIssuedWarnings("11B10101");

            assertThat(result).isEmpty();
            verify(eventRepository, never()).findLatestByWarningRegionCodes(anyList());
        }
    }

    @Nested
    @DisplayName("조회 및 변환")
    class LoadAndConvert {

        @Test
        @DisplayName("LIFTED 이벤트는 필터링된다")
        void liftedEventFiltered() {
            when(mappingRepository.findWarningRegionCodes("11B10101"))
                    .thenReturn(List.of("L1051000"));

            WarningEvent activeEvent = new WarningEvent(
                    "L1051000", WarningKind.RAIN, WarningLevel.ADVISORY, null,
                    WarningEventType.NEW,
                    LocalDateTime.of(2026, 3, 30, 12, 0),
                    LocalDateTime.of(2026, 3, 30, 12, 0)
            );
            ReflectionTestUtils.setField(activeEvent, "id", 1L);

            WarningEvent liftedEvent = new WarningEvent(
                    "L1051000", WarningKind.HEAT, WarningLevel.WARNING, null,
                    WarningEventType.LIFTED,
                    LocalDateTime.of(2026, 3, 30, 10, 0),
                    LocalDateTime.of(2026, 3, 30, 10, 0)
            );
            ReflectionTestUtils.setField(liftedEvent, "id", 2L);

            when(eventRepository.findLatestByWarningRegionCodes(List.of("L1051000")))
                    .thenReturn(List.of(activeEvent, liftedEvent));

            List<IssuedWarning> result = service.loadIssuedWarnings("11B10101");

            assertThat(result).hasSize(1);

            IssuedWarning issued = result.get(0);
            assertThat(issued.eventId()).isEqualTo(1L);
            assertThat(issued.kind()).isEqualTo(WarningKind.RAIN);
            assertThat(issued.level()).isEqualTo(WarningLevel.ADVISORY);
            assertThat(issued.prevLevel()).isNull();
            assertThat(issued.eventType()).isEqualTo(WarningEventType.NEW);
            assertThat(issued.announceTime()).isEqualTo(LocalDateTime.of(2026, 3, 30, 12, 0));
            assertThat(issued.effectiveTime()).isEqualTo(LocalDateTime.of(2026, 3, 30, 12, 0));
        }
    }

    @Nested
    @DisplayName("캐시 동작")
    class CacheBehavior {

        @Test
        @DisplayName("같은 regionId 두 번 호출 시 repository 1회만 호출")
        void cacheHit() {
            when(mappingRepository.findWarningRegionCodes("11B10101"))
                    .thenReturn(List.of("L1051000"));
            when(eventRepository.findLatestByWarningRegionCodes(List.of("L1051000")))
                    .thenReturn(List.of());

            service.loadIssuedWarnings("11B10101");
            service.loadIssuedWarnings("11B10101");

            verify(mappingRepository, times(1)).findWarningRegionCodes("11B10101");
        }

        @Test
        @DisplayName("TTL 만료 후 재조회")
        void cacheExpired() {
            when(mappingRepository.findWarningRegionCodes("11B10101"))
                    .thenReturn(List.of("L1051000"));
            when(eventRepository.findLatestByWarningRegionCodes(List.of("L1051000")))
                    .thenReturn(List.of());

            service.loadIssuedWarnings("11B10101");

            clock.advance(Duration.ofMinutes(51));

            service.loadIssuedWarnings("11B10101");

            verify(mappingRepository, times(2)).findWarningRegionCodes("11B10101");
        }

        @Test
        @DisplayName("다른 regionId는 캐시 분리")
        void separateCachePerRegion() {
            when(mappingRepository.findWarningRegionCodes("11B10101"))
                    .thenReturn(List.of("L1051000"));
            when(mappingRepository.findWarningRegionCodes("11B20201"))
                    .thenReturn(List.of("L1090800"));
            when(eventRepository.findLatestByWarningRegionCodes(anyList()))
                    .thenReturn(List.of());

            service.loadIssuedWarnings("11B10101");
            service.loadIssuedWarnings("11B20201");

            verify(mappingRepository, times(1)).findWarningRegionCodes("11B10101");
            verify(mappingRepository, times(1)).findWarningRegionCodes("11B20201");
        }
    }

    static class MutableClock extends Clock {

        private Instant instant;
        private final ZoneId zone;

        MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void advance(Duration duration) {
            this.instant = this.instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(this.instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}