package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.readmodel.WarningView;
import com.github.yun531.climate.warning.domain.model.WarningEventType;
import com.github.yun531.climate.warning.domain.model.WarningKind;
import com.github.yun531.climate.warning.domain.model.WarningLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarningViewCacheManagerTest {

    private WarningViewCacheManager cacheManager;

    private static final LocalDateTime ANNOUNCE_TIME = LocalDateTime.of(2026, 4, 26, 10, 0);

    @BeforeEach
    void setUp() {
        cacheManager = new WarningViewCacheManager();
    }

    @Test
    @DisplayName("put 후 get -- 동일 리스트 반환")
    void putThenGet() {
        List<WarningView> views = List.of(buildView());
        cacheManager.putWarningViews("R1", views);

        assertThat(cacheManager.getWarningViews("R1")).isSameAs(views);
    }

    @Test
    @DisplayName("미등록 regionId -- null")
    void miss() {
        assertThat(cacheManager.getWarningViews("R1")).isNull();
    }

    @Test
    @DisplayName("빈 리스트도 캐싱 -- get 시 non-null 빈 리스트 반환")
    void emptyListIsCached() {
        cacheManager.putWarningViews("R1", List.of());

        List<WarningView> result = cacheManager.getWarningViews("R1");

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("invalidate 후 모든 키 미스")
    void invalidate_clearsAll() {
        cacheManager.putWarningViews("R1", List.of(buildView()));
        cacheManager.putWarningViews("R2", List.of());

        cacheManager.invalidate();

        assertThat(cacheManager.getWarningViews("R1")).isNull();
        assertThat(cacheManager.getWarningViews("R2")).isNull();
    }

    // ==================== helper ====================

    private WarningView buildView() {
        return new WarningView(
                1L, WarningKind.RAIN, WarningLevel.ADVISORY, null,
                WarningEventType.NEW, ANNOUNCE_TIME, ANNOUNCE_TIME);
    }
}