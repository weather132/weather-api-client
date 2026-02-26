package com.github.yun531.climate.midTerm.infra.persistence;

import com.github.yun531.climate.midTerm.domain.pop.MidPop;
import com.github.yun531.climate.midTerm.domain.pop.MidPopRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class MidPopRepositoryImpl implements MidPopRepository {

    private final JdbcTemplate jdbcTemplate;

    public MidPopRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
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

                    if (midPop.getPop() == null) {
                        ps.setNull(5, Types.INTEGER);
                    } else {
                        ps.setInt(5, midPop.getPop());
                    }
                }
        );
    }
}