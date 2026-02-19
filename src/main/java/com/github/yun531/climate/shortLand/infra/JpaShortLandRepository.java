package com.github.yun531.climate.shortLand.infra;

import com.github.yun531.climate.shortLand.domain.ShortLand;
import com.github.yun531.climate.shortLand.domain.ShortLandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class JpaShortLandRepository implements ShortLandRepository {

    private final DataJpaShortLandRepository dataJpaShortLandRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JpaShortLandRepository(DataJpaShortLandRepository dataJpaShortLandRepository, JdbcTemplate jdbcTemplate) {
        this.dataJpaShortLandRepository = dataJpaShortLandRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ShortLand save(ShortLand shortLand) {
        return dataJpaShortLandRepository.save(shortLand);
    }

    @Override
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
                    ps.setLong(4, shortLand.getCityRegionCodeId());

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
