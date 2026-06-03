package com.github.yun531.climate.airQuality.infra.remote;

import com.github.yun531.climate.sidoRegionCode.SidoRegionCode;
import com.github.yun531.climate.sidoRegionCode.SidoRegionCodeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * sido_region_code 코드(소문자 영문) → id 룩업 캐시.
 * 시드 17개는 부팅 후 불변이므로 @PostConstruct 로 한 번 적재해 고정한다.
 * 수집 경로(AirKoreaItem 변환)에서 시도 코드 → id 변환에 사용.
 */
@Component
public class SidoRegionCodeCache {

    private final SidoRegionCodeRepository repository;
    private Map<String, Long> codeToId;

    public SidoRegionCodeCache(SidoRegionCodeRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    void loadAll() {
        codeToId = repository.findAll().stream()
                .collect(toUnmodifiableMap(SidoRegionCode::getCode, SidoRegionCode::getId));
    }

    public Long findIdByCode(String code) {
        Long id = codeToId.get(code);
        if (id == null) {
            throw new IllegalStateException("Unknown sido code: " + code);
        }
        return id;
    }
}