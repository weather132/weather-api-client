package com.github.yun531.climate.warning.application;

import com.github.yun531.climate.common.cache.CacheEntry;
import com.github.yun531.climate.common.cache.KeyCache;
import com.github.yun531.climate.common.time.TimeUtil;
import com.github.yun531.climate.warning.contract.IssuedWarning;
import com.github.yun531.climate.warning.contract.IssuedWarningReader;
import com.github.yun531.climate.warning.domain.model.WarningEvent;
import com.github.yun531.climate.warning.domain.repository.RegionCodeMappingRepository;
import com.github.yun531.climate.warning.domain.repository.WarningEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class IssuedWarningLoadService implements IssuedWarningReader {

    private final RegionCodeMappingRepository mappingRepository;
    private final WarningEventRepository eventRepository;
    private final Clock clock;
    private final int ttlMinutes;

    private final KeyCache<List<IssuedWarning>> cache = new KeyCache<>();

    public IssuedWarningLoadService(
            RegionCodeMappingRepository mappingRepository,
            WarningEventRepository eventRepository,
            Clock clock,
            @Value("${notification.warning.cache-ttl-minutes:50}") int ttlMinutes
    ) {
        this.mappingRepository = mappingRepository;
        this.eventRepository = eventRepository;
        this.clock = clock;
        this.ttlMinutes = ttlMinutes;
    }

    @Override
    public List<IssuedWarning> loadIssuedWarnings(String regionId) {
        if (regionId == null || regionId.isBlank()) return List.of();

        LocalDateTime now = TimeUtil.truncateToMinutes(LocalDateTime.now(clock));

        CacheEntry<List<IssuedWarning>> entry = cache.getOrCompute(
                regionId,
                now,
                ttlMinutes,
                () -> {
                    List<IssuedWarning> result = doLoad(regionId);
                    return result == null ? null : new CacheEntry<>(result, now);
                }
        );

        return (entry == null || entry.value() == null) ? List.of() : entry.value();
    }

    private List<IssuedWarning> doLoad(String regionId) {
        List<String> warningRegionCodes = mappingRepository.findWarningRegionCodes(regionId);
        if (warningRegionCodes.isEmpty()) return List.of();

        List<WarningEvent> latestEvents = eventRepository.findLatestByWarningRegionCodes(warningRegionCodes);

        return latestEvents.stream()
                .filter(WarningEvent::isActive)
                .map(this::toIssuedWarning)
                .toList();
    }

    private IssuedWarning toIssuedWarning(WarningEvent e) {
        return new IssuedWarning(
                e.getId(),
                e.getKind(),
                e.getLevel(),
                e.getPrevLevel(),
                e.getEventType(),
                e.getAnnounceTime(),
                e.getEffectiveTime()
        );
    }
}