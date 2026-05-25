package com.github.yun531.climate.airQuality.infra.remote;

import com.github.yun531.climate.sidoRegionCode.SidoRegionCode;
import com.github.yun531.climate.sidoRegionCode.SidoRegionCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SidoRegionCodeCache")
class SidoRegionCodeCacheTest {

    private SidoRegionCodeCache cache;

    @BeforeEach
    void setUp() {
        SidoRegionCode seoul = sido(1L, "seoul");
        SidoRegionCode busan = sido(2L, "busan");
        SidoRegionCode jeju  = sido(3L, "jeju");

        SidoRegionCodeRepository repository = mock(SidoRegionCodeRepository.class);
        when(repository.findAll()).thenReturn(List.of(seoul, busan, jeju));

        cache = new SidoRegionCodeCache(repository);
        cache.loadAll();   // @PostConstruct 수동 호출
    }

    @Nested
    @DisplayName("findIdByCode")
    class FindIdByCode {

        @Test
        @DisplayName("적재된 코드 -> id 반환")
        void knownCode_returnsId() {
            assertThat(cache.findIdByCode("seoul")).isEqualTo(1L);
            assertThat(cache.findIdByCode("busan")).isEqualTo(2L);
            assertThat(cache.findIdByCode("jeju")).isEqualTo(3L);
        }

        @Test
        @DisplayName("알 수 없는 코드 -> fail-fast (IllegalStateException)")
        void unknownCode_throws() {
            assertThatThrownBy(() -> cache.findIdByCode("atlantis"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("atlantis");
        }
    }

    private SidoRegionCode sido(Long id, String code) {
        SidoRegionCode entity = mock(SidoRegionCode.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getCode()).thenReturn(code);
        return entity;
    }
}