package com.github.yun531.climate.midTerm.infra.persistence;

import com.github.yun531.climate.midTerm.domain.temperature.MidTemperature;
import com.github.yun531.climate.midTerm.domain.temperature.MidTemperatureRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class MidTemperatureRepositoryImpl implements MidTemperatureRepository {

    private final JdbcTemplate jdbcTemplate;

    public MidTemperatureRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
                    ps.setObject(2, midTemp.getAnnounceTime());
                    ps.setObject(3, midTemp.getEffectiveTime());
                    ps.setLong(4, midTemp.getRegionCode().getId());

                    if (midTemp.getMaxTemp() == null) {
                        ps.setNull(5, Types.INTEGER);
                    }
                    else {
                        ps.setInt(5, midTemp.getMaxTemp());
                    }

                    if (midTemp.getMinTemp() == null) {
                        ps.setNull(6, Types.INTEGER);
                    }
                    else {
                        ps.setInt(6, midTemp.getMinTemp());
                    }
                });
    }
}