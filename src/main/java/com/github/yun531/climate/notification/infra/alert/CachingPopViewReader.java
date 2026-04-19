package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.readmodel.PopView;
import com.github.yun531.climate.notification.domain.readmodel.PopViewReader;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * {@link PopViewReader} 구현체.
 */
@Component
@RequiredArgsConstructor
public class CachingPopViewReader implements PopViewReader {

    private final PopCacheManager cache;
    private final PopViewComposer composer;

    @Override
    @Nullable
    public PopView loadCurrent(String regionId) {
        PopView cached = cache.getCurrent(regionId);
        if (cached != null) return cached;

        PopView view = composer.compose(regionId);
        if (view != null) {
            cache.putCurrent(regionId, view);
        }
        return view;
    }

    @Override
    @Nullable
    public PopView loadPrevious(String regionId) {
        // previous는 rotate에 의해 이전 주기 데이터가 들어있음.
        // 캐시 미스(서버 재시작 직후 등)면 null → RainOnsetDetector가 null 처리.
        return cache.getPrevious(regionId);
    }
}