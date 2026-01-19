package com.github.yun531.climate.repository;

import com.github.yun531.climate.entity.ShortLand;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class ShortLandBatchRepository {
    private final JdbcTemplate jdbcTemplate;

    public ShortLandBatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveAll(List<ShortLand> shortLands) {
        final String sql = "INSERT INTO short_land VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                shortLands,
                shortLands.size(),
                (ps, shortLand) -> {
                    ps.setNull(1, Types.BIGINT);
                    ps.setObject(2, shortLand.getAnnounceTime());
                    ps.setObject(3, shortLand.getEffectiveTime());
                    ps.setLong(4, shortLand.getRegionCode().getId());

                    if (shortLand.getPop() == null) {
                        ps.setNull(5, Types.INTEGER);
                    } else {
                        ps.setInt(5, shortLand.getPop());
                    }

                    if (shortLand.getTemp() == null) {
                        ps.setNull(6, Types.INTEGER);
                    } else {
                        ps.setInt(6, shortLand.getTemp());
                    }

                    if (shortLand.getRainType() == null) {
                        ps.setNull(7, Types.INTEGER);
                    } else  {
                        ps.setInt(7, shortLand.getRainType());
                    }
                }

        );
    }
}
