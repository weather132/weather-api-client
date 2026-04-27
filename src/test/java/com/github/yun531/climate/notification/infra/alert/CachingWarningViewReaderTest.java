package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.readmodel.WarningView;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachingWarningViewReaderTest {

    @Mock WarningViewCacheManager cache;
    @Mock RegionCodeMappingRepository mappingRepository;
    @Mock WarningEventRepository eventRepository;

    private CachingWarningViewReader reader;

    private static final String REGION_ID = "11B10101";
    private static final LocalDateTime T1 = LocalDateTime.of(2026, 4, 15, 10, 0);
    private static final LocalDateTime T2 = LocalDateTime.of(2026, 4, 16, 10, 0);

    @BeforeEach
    void setUp() {
        reader = new CachingWarningViewReader(cache, mappingRepository, eventRepository);
    }

    @Nested
    @DisplayName("loadWarningViews")
    class LoadWarningViews {

        @Test
        @DisplayName("캐시 히트 -- DB 미호출, 캐시 결과 반환")
        void cacheHit_returnsWithoutDbCall() {
            List<WarningView> cached = List.of(buildView(1L, "RAIN", "ADVISORY", T1));
            when(cache.getWarningViews(REGION_ID)).thenReturn(cached);

            List<WarningView> result = reader.loadWarningViews(REGION_ID);

            assertThat(result).isSameAs(cached);
            verify(mappingRepository, never()).findWarningRegionCodes(anyString());
            verify(eventRepository, never()).findLatestByWarningRegionCodes(anyList());
            verify(cache, never()).putWarningViews(anyString(), anyList());
        }

        @Test
        @DisplayName("캐시 미스 -- 매핑/이벤트 조회 후 캐시 저장")
        void cacheMiss_fetchesAndCaches() {
            when(cache.getWarningViews(REGION_ID)).thenReturn(null);
            when(mappingRepository.findWarningRegionCodes(REGION_ID))
                    .thenReturn(List.of("L1100100"));
            when(eventRepository.findLatestByWarningRegionCodes(List.of("L1100100")))
                    .thenReturn(List.of(buildEvent(1L, WarningKind.RAIN, WarningLevel.ADVISORY,
                            null, WarningEventType.NEW, T1)));

            List<WarningView> result = reader.loadWarningViews(REGION_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).kind()).isEqualTo("RAIN");
            verify(cache).putWarningViews(REGION_ID, result);
        }

        @Test
        @DisplayName("null regionId -- 빈 결과, DB/캐시 모두 미호출")
        void nullRegionId_earlyReturn() {
            assertThat(reader.loadWarningViews(null)).isEmpty();

            verify(cache, never()).getWarningViews(any());
            verify(mappingRepository, never()).findWarningRegionCodes(any());
        }

        @Test
        @DisplayName("blank regionId -- 빈 결과, DB/캐시 모두 미호출")
        void blankRegionId_earlyReturn() {
            assertThat(reader.loadWarningViews("  ")).isEmpty();

            verify(cache, never()).getWarningViews(any());
            verify(mappingRepository, never()).findWarningRegionCodes(any());
        }
    }

    @Nested
    @DisplayName("fetch 경로 동작")
    class FetchPath {

        @Test
        @DisplayName("매핑 없음 -- 이벤트 조회 안 함, 빈 리스트")
        void noMapping_skipsEventQuery() {
            when(cache.getWarningViews(REGION_ID)).thenReturn(null);
            when(mappingRepository.findWarningRegionCodes(REGION_ID)).thenReturn(List.of());

            assertThat(reader.loadWarningViews(REGION_ID)).isEmpty();
            verify(eventRepository, never()).findLatestByWarningRegionCodes(any());
            verify(cache).putWarningViews(REGION_ID, List.of());
        }

        @Test
        @DisplayName("LIFTED 이벤트는 필터링됨")
        void liftedEventFiltered() {
            when(cache.getWarningViews(REGION_ID)).thenReturn(null);
            when(mappingRepository.findWarningRegionCodes(REGION_ID))
                    .thenReturn(List.of("L1100100"));
            when(eventRepository.findLatestByWarningRegionCodes(List.of("L1100100")))
                    .thenReturn(List.of(
                            buildEvent(1L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                                    WarningEventType.NEW, T1),
                            buildEvent(2L, WarningKind.HEAT, WarningLevel.ADVISORY, null,
                                    WarningEventType.LIFTED, T1)
                    ));

            List<WarningView> result = reader.loadWarningViews(REGION_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).kind()).isEqualTo("RAIN");
        }

        @Test
        @DisplayName("의미 중복 통합 -- 4개 코드의 동일 (kind, level) → 1건")
        void semanticDedup() {
            when(cache.getWarningViews(REGION_ID)).thenReturn(null);
            when(mappingRepository.findWarningRegionCodes(REGION_ID))
                    .thenReturn(List.of("L1100100", "L1100200", "L1100300", "L1100400"));
            when(eventRepository.findLatestByWarningRegionCodes(anyList()))
                    .thenReturn(List.of(
                            buildEvent(321L, WarningKind.DRY, WarningLevel.ADVISORY, null,
                                    WarningEventType.NEW, T1),
                            buildEvent(325L, WarningKind.DRY, WarningLevel.ADVISORY, null,
                                    WarningEventType.NEW, T1),
                            buildEvent(330L, WarningKind.DRY, WarningLevel.ADVISORY, null,
                                    WarningEventType.NEW, T1),
                            buildEvent(334L, WarningKind.DRY, WarningLevel.ADVISORY, null,
                                    WarningEventType.NEW, T2)
                    ));

            List<WarningView> result = reader.loadWarningViews(REGION_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).eventId()).isEqualTo(334L);
            assertThat(result.get(0).announceTime()).isEqualTo(T2);
        }

        @Test
        @DisplayName("결과는 eventId 오름차순으로 정렬")
        void resultSortedByEventIdAsc() {
            when(cache.getWarningViews(REGION_ID)).thenReturn(null);
            when(mappingRepository.findWarningRegionCodes(REGION_ID))
                    .thenReturn(List.of("L1100100"));
            when(eventRepository.findLatestByWarningRegionCodes(any()))
                    .thenReturn(List.of(
                            buildEvent(30L, WarningKind.HEAT, WarningLevel.WARNING, null,
                                    WarningEventType.NEW, T1),
                            buildEvent(10L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                                    WarningEventType.NEW, T1),
                            buildEvent(20L, WarningKind.DRY, WarningLevel.ADVISORY, null,
                                    WarningEventType.NEW, T1)
                    ));

            List<WarningView> result = reader.loadWarningViews(REGION_ID);

            assertThat(result).extracting(WarningView::eventId)
                    .containsExactly(10L, 20L, 30L);
        }
    }

    @Nested
    @DisplayName("엔티티 → 읽기모델 변환")
    class ToWarningView {

        @Test
        @DisplayName("enum → String 변환 + prevLevel null 처리")
        void enumToStringAndNullPrevLevel() {
            when(cache.getWarningViews(REGION_ID)).thenReturn(null);
            when(mappingRepository.findWarningRegionCodes(REGION_ID))
                    .thenReturn(List.of("L1100100"));
            when(eventRepository.findLatestByWarningRegionCodes(any()))
                    .thenReturn(List.of(
                            buildEvent(42L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                                    WarningEventType.NEW, T1)
                    ));

            WarningView v = reader.loadWarningViews(REGION_ID).get(0);

            assertThat(v.eventId()).isEqualTo(42L);
            assertThat(v.kind()).isEqualTo("RAIN");
            assertThat(v.level()).isEqualTo("ADVISORY");
            assertThat(v.prevLevel()).isNull();
            assertThat(v.eventType()).isEqualTo("NEW");
        }

        @Test
        @DisplayName("prevLevel 존재 시 .name() 변환")
        void prevLevelPresent() {
            when(cache.getWarningViews(REGION_ID)).thenReturn(null);
            when(mappingRepository.findWarningRegionCodes(REGION_ID))
                    .thenReturn(List.of("L1100100"));
            when(eventRepository.findLatestByWarningRegionCodes(any()))
                    .thenReturn(List.of(
                            buildEvent(99L, WarningKind.HEAT, WarningLevel.WARNING,
                                    WarningLevel.ADVISORY, WarningEventType.UPGRADED, T1)
                    ));

            WarningView v = reader.loadWarningViews(REGION_ID).get(0);

            assertThat(v.prevLevel()).isEqualTo("ADVISORY");
            assertThat(v.eventType()).isEqualTo("UPGRADED");
        }
    }

    // ==================== helpers ====================

    private WarningEvent buildEvent(long id, WarningKind kind, WarningLevel level,
                                    WarningLevel prevLevel, WarningEventType eventType,
                                    LocalDateTime time) {
        WarningEvent event = new WarningEvent(
                "L1100100", kind, level, prevLevel, eventType, time, time);
        ReflectionTestUtils.setField(event, "id", id);
        return event;
    }

    private WarningView buildView(long eventId, String kind, String level, LocalDateTime time) {
        return new WarningView(eventId, kind, level, null, "NEW", time, time);
    }
}