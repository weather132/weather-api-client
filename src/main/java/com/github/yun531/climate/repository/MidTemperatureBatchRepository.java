package com.github.yun531.climate.repository;

import com.github.yun531.climate.entity.MidTemperature;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class MidTemperatureBatchRepository {
    private final JdbcTemplate jdbcTemplate;

    public MidTemperatureBatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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
                    } else  {
                        ps.setLong(5, midTemp.getMaxTemp());
                    }

                    if (midTemp.getMinTemp() == null) {
                        ps.setNull(6, Types.INTEGER);
                    }  else  {
                        ps.setLong(6, midTemp.getMinTemp());
                    }
                });
    }
}
