package com.github.yun531.climate.repository;

import com.github.yun531.climate.entity.MidPop;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class MidPopBatchRepository {
    private final JdbcTemplate jdbcTemplate;

    public MidPopBatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveAll(List<MidPop> midPops) {
        final String sql = "INSERT INTO mid_pop VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                midPops,
                midPops.size(),
                (ps, midPop) -> {
                    ps.setNull(1, Types.BIGINT);
                    ps.setObject(2, midPop.getAnnounceTime());
                    ps.setObject(3, midPop.getEffectiveTime());
                    ps.setLong(4, midPop.getRegionCode().getId());
                    ps.setInt(5, midPop.getPop());
                }
        );
    }
}
