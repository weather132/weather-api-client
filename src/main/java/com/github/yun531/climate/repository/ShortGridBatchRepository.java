package com.github.yun531.climate.repository;

import com.github.yun531.climate.entity.ShortGrid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class ShortGridBatchRepository {
    private final JdbcTemplate jdbcTemplate;

    public ShortGridBatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveAll(List<ShortGrid> grids) {
        final String sql = "INSERT INTO short_grid VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                grids,
                grids.size(),
                (ps, grid) -> {
                    ps.setNull(1, Types.BIGINT);
                    ps.setObject(2, grid.getAnnounceTime());
                    ps.setObject(3, grid.getEffectiveTime());
                    ps.setInt(4, grid.getX());
                    ps.setInt(5, grid.getY());

                    if (grid.getPop() == null) {
                        ps.setNull(6, Types.INTEGER);
                    } else {
                        ps.setInt(6, grid.getPop());
                    }

                    if (grid.getTemp() == null) {
                        ps.setNull(7, Types.INTEGER);
                    } else {
                        ps.setInt(7, grid.getTemp());
                    }
                });
    }
}
