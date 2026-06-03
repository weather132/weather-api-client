package com.github.yun531.climate.airQuality.domain;

import java.util.List;

/**
 * AirKorea 수집 포트. 한 항목(itemCode)만 채워진 AirQuality 리스트를 반환
 * (나머지 PM 컬럼은 null).
 */
public interface AirQualityClient {
    List<AirQuality> fetchLatest(PmItemCode itemCode);
}