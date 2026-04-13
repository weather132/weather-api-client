package com.github.yun531.climate.midTemperature.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
import com.github.yun531.climate.midTemperature.domain.MidTemperatureRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class MidTemperatureRepositoryImpl implements MidTemperatureRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JpaMidTemperatureRepository jpaMidTemperatureRepository;

    public MidTemperatureRepositoryImpl(JdbcTemplate jdbcTemplate, JpaMidTemperatureRepository jpaMidTemperatureRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.jpaMidTemperatureRepository = jpaMidTemperatureRepository;
    }

    @Override
    public void saveAll(List<MidTemperature> midTemps) {
        final String sql = "INSERT INTO mid_temperature VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                midTemps,
                midTemps.size(),
                (ps, midTemp) -> {
                    ps.setNull(1, Types.BIGINT);
                    ps.setObject(2, midTemp.getAnnounceTime().getTime());
                    ps.setObject(3, midTemp.getEffectiveTime());
                    ps.setLong(4, midTemp.getCityRegionCodeId());

                    if (midTemp.getMaxTemp() == null) {
                        ps.setNull(5, Types.INTEGER);
                    } else {
                        ps.setInt(5, midTemp.getMaxTemp());
                    }

                    if (midTemp.getMinTemp() == null) {
                        ps.setNull(6, Types.INTEGER);
                    } else {
                        ps.setInt(6, midTemp.getMinTemp());
                    }
                });
    }

    @Override
    public MidTemperature findRecent(CityRegionCode cityRegionCode, LocalDateTime effectiveTime) {
        return findRecentAll(cityRegionCode, List.of(effectiveTime))
                .getOrDefault(effectiveTime, new MidTemperature(null, null, null, null, null));
    }

    @Override
    public Map<LocalDateTime, MidTemperature> findRecentAll(
            CityRegionCode regionCode, List<LocalDateTime> effectiveTimes
    ) {
        if (effectiveTimes == null || effectiveTimes.isEmpty()) return Map.of();

        return jpaMidTemperatureRepository
                .findRecentByRegionAndEffectiveTimes(regionCode.getId(), effectiveTimes)
                .stream()
                .collect(Collectors.toMap(MidTemperature::getEffectiveTime, Function.identity()));
    }

    @Override
    public List<MidTemperature> findAll() {
        return jpaMidTemperatureRepository.findAll();
    }
}