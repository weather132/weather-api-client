package com.github.yun531.climate.shortLand.application;

import com.github.yun531.climate.cityRegionCode.reference.CityRegionCodeRepository;
import com.github.yun531.climate.shortLand.domain.ShortLand;
import com.github.yun531.climate.shortLand.domain.ShortLandClient;
import com.github.yun531.climate.shortLand.domain.ShortLandRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShortLandService {
    private final ShortLandClient client;
    private final CityRegionCodeRepository cityRegionCodeRepository;
    private final ShortLandRepository shortLandRepository;

    public ShortLandService(ShortLandClient client, CityRegionCodeRepository cityRegionCodeRepository, ShortLandRepository shortLandRepository) {
        this.client = client;
        this.cityRegionCodeRepository = cityRegionCodeRepository;
        this.shortLandRepository = shortLandRepository;
    }

    public void updateShortland() {
        List<ShortLand> shortLands = cityRegionCodeRepository.findAll().stream()
                .map(client::requestShortLand)
                .flatMap(List::stream)
                .toList();

        shortLandRepository.saveAll(shortLands);
    }
}
