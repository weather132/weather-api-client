package com.github.yun531.climate.midLand.application;

import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandClient;
import com.github.yun531.climate.midLand.domain.MidLandRepository;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MidLandService {
    private final MidLandClient client;
    private final MidLandRepository midLandRepository;
    private final ProvinceRegionCodeRepository provinceRegionCodeRepository;

    public MidLandService(MidLandClient client, MidLandRepository midLandRepository, ProvinceRegionCodeRepository provinceRegionCodeRepository) {
        this.client = client;
        this.midLandRepository = midLandRepository;
        this.provinceRegionCodeRepository = provinceRegionCodeRepository;
    }

    public void updateMidland() {
        List<MidLand> midLands = client.requestMidLands(new MidAnnounceTime(LocalDateTime.now()), provinceRegionCodeRepository.findAll());
        midLandRepository.saveAll(midLands);
    }
}
