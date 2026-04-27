package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.readmodel.WarningView;
import com.github.yun531.climate.notification.domain.readmodel.WarningViewReader;
import com.github.yun531.climate.notification.domain.readmodel.WarningViewSelector;
import com.github.yun531.climate.warning.domain.model.WarningEvent;
import com.github.yun531.climate.warning.domain.repository.RegionCodeMappingRepository;
import com.github.yun531.climate.warning.domain.repository.WarningEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CachingWarningViewReader implements WarningViewReader {

    private final WarningViewCacheManager cache;
    private final RegionCodeMappingRepository mappingRepository;
    private final WarningEventRepository eventRepository;

    @Override
    public List<WarningView> loadWarningViews(String regionId) {
        if (regionId == null || regionId.isBlank()) return List.of();

        List<WarningView> cached = cache.getWarningViews(regionId);
        if (cached != null) return cached;

        List<WarningView> warningViews = fetchWarningViews(regionId);
        cache.putWarningViews(regionId, warningViews);
        return warningViews;
    }

    private List<WarningView> fetchWarningViews(String regionId) {
        List<String> codes = mappingRepository.findWarningRegionCodes(regionId);
        if (codes.isEmpty()) return List.of();

        List<WarningView> rawWarningViews = loadLatestViews(codes);
        return dedupAndSort(rawWarningViews);
    }

    private List<WarningView> loadLatestViews(List<String> warningRegionCodes) {
        return eventRepository.findLatestByWarningRegionCodes(warningRegionCodes).stream()
                .filter(WarningEvent::isActive)
                .map(this::toWarningView)
                .toList();
    }

    private List<WarningView> dedupAndSort(List<WarningView> rawViews) {
        return WarningViewSelector.pickLatestPerKindAndLevel(rawViews).stream()
                .sorted(Comparator.comparingLong(WarningView::eventId))
                .toList();
    }

    private WarningView toWarningView(WarningEvent e) {
        return new WarningView(
                e.getId(),
                e.getKind().name(),
                e.getLevel().name(),
                e.getPrevLevel() != null ? e.getPrevLevel().name() : null,
                e.getEventType().name(),
                e.getAnnounceTime(),
                e.getEffectiveTime()
        );
    }
}