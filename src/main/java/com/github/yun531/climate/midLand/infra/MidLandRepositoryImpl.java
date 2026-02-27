package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class MidLandRepositoryImpl implements MidLandRepository {
    private final JdbcTemplate  jdbcTemplate;

    public MidLandRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAll(List<MidLand> midLands) {
        final String sql = "INSERT INTO mid_pop VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                midLands,
                midLands.size(),
                (ps, midPop) -> {
                    ps.setNull(1, Types.BIGINT);
                    ps.setObject(2, midPop.getAnnounceTime().getTime());
                    ps.setObject(3, midPop.getEffectiveTime());
                    ps.setLong(4, midPop.getProvinceRegionCodeId());
                    ps.setInt(5, midPop.getPop());
                }
        );
    }
}
