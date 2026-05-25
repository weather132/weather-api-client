package com.github.yun531.climate.airQuality.application;

import com.github.yun531.climate.airQuality.domain.AirQuality;
import com.github.yun531.climate.airQuality.domain.AirQualityClient;
import com.github.yun531.climate.airQuality.domain.AirQualityRepository;
import com.github.yun531.climate.airQuality.domain.PmItemCode;
import com.github.yun531.climate.common.event.AirQualityRefreshedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AirQualityCollectService")
class AirQualityCollectServiceTest {

    private static final LocalDateTime T11 = LocalDateTime.of(2026, 5, 21, 11, 0);
    private static final LocalDateTime T10 = LocalDateTime.of(2026, 5, 21, 10, 0);

    @Mock AirQualityClient client;
    @Mock AirQualityRepository repository;
    @Mock ApplicationEventPublisher eventPublisher;

    @Captor ArgumentCaptor<List<AirQuality>> savedCaptor;
    @Captor ArgumentCaptor<AirQualityRefreshedEvent> eventCaptor;

    private AirQualityCollectService service;

    @BeforeEach
    void setUp() {
        service = new AirQualityCollectService(client, repository, eventPublisher);
    }

    @Nested
    @DisplayName("교집합 페어링")
    class Pairing {

        @Test
        @DisplayName("PM10/PM25 둘 다 있는 (시도,시각)만 저장 -- 한쪽 누락분 탈락")
        void onlyIntersection() {
            // PM10: 시도1@11, 시도2@11, 시도1@10  / PM25: 시도1@11, 시도2@11 (시도1@10 누락)
            when(client.fetchLatest(PmItemCode.PM10)).thenReturn(List.of(
                    new AirQuality(1L, T11, 7, null),
                    new AirQuality(2L, T11, 25, null),
                    new AirQuality(1L, T10, 6, null)));
            when(client.fetchLatest(PmItemCode.PM25)).thenReturn(List.of(
                    new AirQuality(1L, T11, null, 4),
                    new AirQuality(2L, T11, null, 12)));
            when(repository.findLatestAnnounceTime()).thenReturn(Optional.empty());

            service.collect();

            verify(repository).saveAll(savedCaptor.capture());
            List<AirQuality> saved = savedCaptor.getValue();

            // 교집합 2건 (시도1@11, 시도2@11), 시도1@10 은 PM25 없어 탈락
            assertThat(saved).hasSize(2);
            assertThat(saved).allMatch(aq -> aq.getPm10() != null && aq.getPm25() != null);
            assertThat(saved).noneMatch(aq -> aq.getAnnounceTime().equals(T10));
        }
    }

    @Nested
    @DisplayName("신규 필터 (onlyNewer)")
    class OnlyNewer {

        @Test
        @DisplayName("마지막 적재 시각 이후만 저장")
        void filtersByLastSaved() {
            when(client.fetchLatest(PmItemCode.PM10)).thenReturn(List.of(
                    new AirQuality(1L, T11, 7, null),
                    new AirQuality(1L, T10, 6, null)));
            when(client.fetchLatest(PmItemCode.PM25)).thenReturn(List.of(
                    new AirQuality(1L, T11, null, 4),
                    new AirQuality(1L, T10, null, 3)));
            // 마지막 적재 10:00 -> 11:00 만 신규
            when(repository.findLatestAnnounceTime()).thenReturn(Optional.of(T10));

            service.collect();

            verify(repository).saveAll(savedCaptor.capture());
            assertThat(savedCaptor.getValue()).hasSize(1);
            assertThat(savedCaptor.getValue().get(0).getAnnounceTime()).isEqualTo(T11);
        }

        @Test
        @DisplayName("신규 없음 -> 저장/이벤트 모두 미발생")
        void noNew_noSaveNoEvent() {
            when(client.fetchLatest(PmItemCode.PM10)).thenReturn(List.of(
                    new AirQuality(1L, T10, 6, null)));
            when(client.fetchLatest(PmItemCode.PM25)).thenReturn(List.of(
                    new AirQuality(1L, T10, null, 3)));
            when(repository.findLatestAnnounceTime()).thenReturn(Optional.of(T10));

            service.collect();

            verify(repository, never()).saveAll(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("이벤트 발행")
    class EventPublish {

        @Test
        @DisplayName("저장 후 최신 시각으로 AirQualityRefreshedEvent 발행")
        void publishesWithLatestTime() {
            when(client.fetchLatest(PmItemCode.PM10)).thenReturn(List.of(
                    new AirQuality(1L, T11, 7, null),
                    new AirQuality(2L, T10, 6, null)));
            when(client.fetchLatest(PmItemCode.PM25)).thenReturn(List.of(
                    new AirQuality(1L, T11, null, 4),
                    new AirQuality(2L, T10, null, 3)));
            when(repository.findLatestAnnounceTime()).thenReturn(Optional.empty());

            service.collect();

            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().announceTime()).isEqualTo(T11);
        }
    }

    @Nested
    @DisplayName("부분 실패")
    class PartialFailure {

        @Test
        @DisplayName("한쪽 itemCode 수집 실패 -> 교집합 비어 미적재")
        void oneItemFails_noSave() {
            when(client.fetchLatest(PmItemCode.PM10)).thenReturn(List.of(
                    new AirQuality(1L, T11, 7, null)));
            when(client.fetchLatest(PmItemCode.PM25))
                    .thenThrow(new RuntimeException("API 실패"));

            service.collect();

            verify(repository, never()).saveAll(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}