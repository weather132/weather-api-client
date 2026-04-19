package com.github.yun531.climate.midCollection.application;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.cityRegionCode.domain.CityRegionCodeRepository;
import com.github.yun531.climate.common.event.MidCollectionRefreshedEvent;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandClient;
import com.github.yun531.climate.midLand.domain.MidLandRepository;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureClient;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureRepository;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MidCollectionServiceTest {

    @Mock MidLandClient midLandClient;
    @Mock MidTemperatureClient midTemperatureClient;
    @Mock MidLandRepository midLandRepository;
    @Mock MidTemperatureRepository midTemperatureRepository;
    @Mock ProvinceRegionCodeRepository provinceRegionCodeRepository;
    @Mock CityRegionCodeRepository cityRegionCodeRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    private MidCollectionService service;

    @BeforeEach
    void setUp() {
        service = new MidCollectionService(
                midLandClient, midTemperatureClient,
                midLandRepository, midTemperatureRepository,
                provinceRegionCodeRepository, cityRegionCodeRepository,
                eventPublisher
        );
    }

    @Test
    @DisplayName("collect: 두 Client 호출 → 두 saveAll → 이벤트 1회 발행")
    void collect_callsBothClientsAndPublishesOneEvent() {
        ProvinceRegionCode prc = mock(ProvinceRegionCode.class);
        CityRegionCode crc = mock(CityRegionCode.class);
        MidLand midLand = mock(MidLand.class);
        MidTemperature midTemp = mock(MidTemperature.class);

        when(provinceRegionCodeRepository.findAll()).thenReturn(List.of(prc));
        when(cityRegionCodeRepository.findAll()).thenReturn(List.of(crc));
        when(midLandClient.requestMidLands(any(), any())).thenReturn(List.of(midLand));
        when(midTemperatureClient.requestMidTemperatures(any(), any())).thenReturn(List.of(midTemp));

        service.collect();

        verify(midLandClient).requestMidLands(any(), eq(List.of(prc)));
        verify(midTemperatureClient).requestMidTemperatures(any(), eq(List.of(crc)));
        verify(midLandRepository).saveAll(List.of(midLand));
        verify(midTemperatureRepository).saveAll(List.of(midTemp));

        ArgumentCaptor<MidCollectionRefreshedEvent> captor =
                ArgumentCaptor.forClass(MidCollectionRefreshedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertThat(captor.getValue().announceTime()).isNotNull();
    }

    @Test
    @DisplayName("collect: 이벤트의 announceTime은 MidAnnounceTime 규칙에 따른 정규화된 시각")
    void collect_eventAnnounceTimeIsNormalized() {
        when(provinceRegionCodeRepository.findAll()).thenReturn(List.of());
        when(cityRegionCodeRepository.findAll()).thenReturn(List.of());
        when(midLandClient.requestMidLands(any(), any())).thenReturn(List.of());
        when(midTemperatureClient.requestMidTemperatures(any(), any())).thenReturn(List.of());

        service.collect();

        ArgumentCaptor<MidCollectionRefreshedEvent> captor =
                ArgumentCaptor.forClass(MidCollectionRefreshedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        // MidAnnounceTime은 분/초/나노를 0으로 정규화
        assertThat(captor.getValue().announceTime().getMinute()).isZero();
        assertThat(captor.getValue().announceTime().getSecond()).isZero();
    }
}