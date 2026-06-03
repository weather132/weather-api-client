package com.github.yun531.climate.airQuality.infra.persistence;

import com.github.yun531.climate.airQuality.domain.AirQuality;
import com.github.yun531.climate.airQuality.domain.AirQualityRepository;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AirQualityRepositoryImpl implements AirQualityRepository {

    private static final String INSERT_SQL = "INSERT INTO air_quality VALUES (?, ?, ?, ?, ?)";

    private final JpaAirQualityRepository jpa;
    private final JdbcTemplate jdbcTemplate;

    public AirQualityRepositoryImpl(JpaAirQualityRepository jpa, JdbcTemplate jdbcTemplate) {
        this.jpa = jpa;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAll(List<AirQuality> measurements) {
        jdbcTemplate.batchUpdate(
                INSERT_SQL,
                measurements,
                measurements.size(),
                (ps, measurement) -> bind(ps, measurement));
    }

    @Override
    public Optional<LocalDateTime> findLatestAnnounceTime() {
        return Optional.ofNullable(jpa.findMaxAnnounceTime());
    }

    @Override
    public Optional<AirQuality> findRecentBySidoWithin(Long sidoId, LocalDateTime from, LocalDateTime to) {
        return jpa.findRecentBySidoWithin(sidoId, from, to, Limit.of(1));
    }

    @Override
    public Optional<AirQuality> findLatestBySido(Long sidoId) {
        return jpa.findLatestBySido(sidoId, Limit.of(1));
    }

    private void bind(PreparedStatement ps, AirQuality measurement) throws SQLException {
        ps.setNull(1, Types.BIGINT);
        ps.setLong(2, measurement.getSidoRegionCodeId());
        ps.setObject(3, measurement.getAnnounceTime());
        bindNullableInt(ps, 4, measurement.getPm10());
        bindNullableInt(ps, 5, measurement.getPm25());
    }

    private void bindNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }
}