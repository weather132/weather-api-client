package com.github.yun531.climate.midTerm.application;

import com.github.yun531.climate.midTerm.domain.MidAnnounceTime;
import com.github.yun531.climate.midTerm.domain.pop.MidPop;
import com.github.yun531.climate.midTerm.domain.pop.MidPopClient;
import com.github.yun531.climate.midTerm.domain.pop.MidPopDraft;
import com.github.yun531.climate.midTerm.domain.pop.MidPopRepository;
import com.github.yun531.climate.midTerm.domain.province.ProvinceRegionCode;
import com.github.yun531.climate.midTerm.domain.province.ProvinceRegionCodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
public class MidPopService {

    private final ProvinceRegionCodeRepository provinceRegionCodeRepository;
    private final MidPopClient client;
    private final MidPopRepository repository;

    public MidPopService(ProvinceRegionCodeRepository provinceRegionCodeRepository,
                         MidPopClient client,
                         MidPopRepository repository) {
        this.provinceRegionCodeRepository = provinceRegionCodeRepository;
        this.client = client;
        this.repository = repository;
    }

    public void updateMidPop() {
        MidAnnounceTime announceTime = new MidAnnounceTime(LocalDateTime.now());

        List<MidPop> entities = provinceRegionCodeRepository.findAll().stream()
                .map(province -> draftsToEntities(province, announceTime,
                        client.requestMidPopDrafts(province.getRegionCode(), announceTime)))
                .flatMap(Collection::stream)
                .toList();

        repository.saveAll(entities);
    }

    private List<MidPop> draftsToEntities(ProvinceRegionCode province,
                                          MidAnnounceTime announceTime,
                                          List<MidPopDraft> drafts) {
        LocalDateTime at = announceTime.getAnnounceTime();
        return drafts.stream()
                .map(d -> new MidPop(at, d.effectiveTime(), province, d.pop()))
                .toList();
    }
}