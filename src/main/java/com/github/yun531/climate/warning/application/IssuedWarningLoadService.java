package com.github.yun531.climate.warning.application;

import com.github.yun531.climate.warning.contract.IssuedWarning;
import com.github.yun531.climate.warning.contract.IssuedWarningReader;
import com.github.yun531.climate.warning.domain.model.WarningEvent;
import com.github.yun531.climate.warning.domain.repository.RegionCodeMappingRepository;
import com.github.yun531.climate.warning.domain.repository.WarningEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IssuedWarningLoadService implements IssuedWarningReader {

    private final RegionCodeMappingRepository mappingRepository;
    private final WarningEventRepository eventRepository;

    public IssuedWarningLoadService(RegionCodeMappingRepository mappingRepository,
                                    WarningEventRepository eventRepository) {
        this.mappingRepository = mappingRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public List<IssuedWarning> loadIssuedWarnings(String regionId) {
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