package com.github.yun531.climate.warning.application;

import com.github.yun531.climate.common.event.WarningRefreshedEvent;
import com.github.yun531.climate.warning.domain.WarningClient;
import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;
import com.github.yun531.climate.warning.domain.repository.WarningCurrentRepository;
import com.github.yun531.climate.warning.domain.repository.WarningEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningCollectServiceTest {

    @Mock WarningClient warningClient;
    @Mock WarningCurrentRepository currentRepository;
    @Mock WarningEventRepository eventRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    private WarningCollectService service;

    private static final LocalDateTime TM = LocalDateTime.of(2026, 4, 26, 10, 10);

    @BeforeEach
    void setUp() {
        service = new WarningCollectService(
                warningClient, currentRepository, eventRepository, eventPublisher);
    }

    @Test
    @DisplayName("변화 감지 -- saveAll 호출 + WarningRefreshedEvent 1회 발행")
    void collect_publishesEvent_whenWarningEventsExist() {
        // previous는 비어 있고 current에 새 특보 1건 -- NEW 이벤트 발생
        when(warningClient.requestCurrentWarnings(TM)).thenReturn(List.of(
                new WarningCurrent("L1100100", WarningKind.RAIN, WarningLevel.ADVISORY, TM, TM)
        ));
        when(currentRepository.findAll()).thenReturn(List.of());

        service.collect(TM);

        verify(eventRepository).saveAll(anyList());

        ArgumentCaptor<WarningRefreshedEvent> captor =
                ArgumentCaptor.forClass(WarningRefreshedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertThat(captor.getValue().announceTime()).isEqualTo(TM);
    }

    @Test
    @DisplayName("변화 없음 -- saveAll 미호출 + 이벤트 미발행")
    void collect_doesNotPublish_whenNoWarningEvents() {
        // previous와 current가 동일 값 -- changeDetector가 빈 리스트 반환
        when(warningClient.requestCurrentWarnings(TM)).thenReturn(List.of(
                new WarningCurrent("L1100100", WarningKind.RAIN, WarningLevel.ADVISORY, TM, TM)
        ));
        when(currentRepository.findAll()).thenReturn(List.of(
                new WarningCurrent("L1100100", WarningKind.RAIN, WarningLevel.ADVISORY, TM, TM)
        ));

        service.collect(TM);

        verify(eventRepository, never()).saveAll(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}