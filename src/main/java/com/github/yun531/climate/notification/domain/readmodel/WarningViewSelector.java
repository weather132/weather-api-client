package com.github.yun531.climate.notification.domain.readmodel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * regionId 단위에서 본 의미상 중복된 WarningView를 통합.
 * 행정구역과 특보 구역의 다대다 매핑으로 발생하는 중복 데이터를 사용자 관점의 단일 이벤트로 통합하여 제공.
 * - 중복 판정: (kind, level) 동일 → 1건으로 통합.
 * - 대표 선택: 그룹 내 announceTime 최대값을 가진 레코드.
 */
public final class WarningViewSelector {

    private WarningViewSelector() {}

    public static List<WarningView> pickLatestPerKindAndLevel(List<WarningView> warningViews) {
        if (warningViews == null || warningViews.isEmpty()) return List.of();

        Map<GroupKey, WarningView> latestByGroup = new LinkedHashMap<>();
        for (WarningView v : warningViews) {
            GroupKey key = new GroupKey(v.kind(), v.level());
            WarningView existing = latestByGroup.get(key);
            if (existing == null || v.announceTime().isAfter(existing.announceTime())) {
                latestByGroup.put(key, v);
            }
        }

        return List.copyOf(latestByGroup.values());
    }

    private record GroupKey(String kind, String level) {}
}