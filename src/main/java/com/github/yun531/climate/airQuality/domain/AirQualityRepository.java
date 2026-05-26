package com.github.yun531.climate.airQuality.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AirQualityRepository {
    void saveAll(List<AirQuality> measurements);
    Optional<LocalDateTime> findLatestAnnounceTime();
    Optional<AirQuality> findRecentBySidoWithin(Long sidoId, LocalDateTime from, LocalDateTime to);
    Optional<AirQuality> findLatestBySido(Long sidoId);
}