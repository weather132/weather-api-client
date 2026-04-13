package com.github.yun531.climate.shortLand.infra;

import com.github.yun531.climate.cityRegionCode.domain.CityRegionCode;
import com.github.yun531.climate.shortLand.domain.ShortLand;
import com.github.yun531.climate.shortLand.domain.ShortLandRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class ShortLandRepositoryImpl implements ShortLandRepository {

    private final JpaShortLandRepository jpaShortLandRepository;
    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public ShortLandRepositoryImpl(JpaShortLandRepository jpaShortLandRepository, JdbcTemplate jdbcTemplate) {
        this.jpaShortLandRepository = jpaShortLandRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ShortLand save(ShortLand shortLand) {
        return jpaShortLandRepository.save(shortLand);
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
                    } else {
                        ps.setInt(7, shortLand.getRainType());
                    }
                }
        );
    }

    @Override
    public ShortLand findRecent(CityRegionCode regionCode, LocalDateTime effectiveTime) {
        return findRecentAll(regionCode, List.of(effectiveTime)).get(effectiveTime);
    }

    @Override
    public Map<LocalDateTime, ShortLand> findRecentAll(CityRegionCode regionCode, List<LocalDateTime> effectiveTimes) {
        if (effectiveTimes == null || effectiveTimes.isEmpty()) return Map.of();

        return jpaShortLandRepository
                .findRecentByRegionAndEffectiveTimes(regionCode.getId(), effectiveTimes)
                .stream()
                .collect(Collectors.toMap(ShortLand::getEffectiveTime, Function.identity()));
    }

    @Override
    public Integer findRecentPop(CityRegionCode regionCode, LocalDateTime effectiveTime) {
        String sql = "select sl.pop from ShortLand sl" +
                " where sl.pop is not null" +
                " and sl.cityRegionCodeId = :cityId" +
                " and sl.effectiveTime = :efTime" +
                " order by sl.announceTime desc" +
                " limit 1";

        return em.createQuery(sql, Integer.class)
                .setParameter("cityId", regionCode.getId())
                .setParameter("efTime", effectiveTime)
                .getSingleResult();
    }

    @Override
    public Integer findRecentMaxTemp(CityRegionCode regionCode, LocalDateTime effectiveTime) {
        return findRecentTemp(regionCode, effectiveTime);
    }

    @Override
    public Integer findRecentMinTemp(CityRegionCode regionCode, LocalDateTime effectiveTime) {
        return findRecentTemp(regionCode, effectiveTime);
    }


    private Integer findRecentTemp(CityRegionCode regionCode, LocalDateTime effectiveTime) {
        String sql = "select sl.temp from ShortLand sl" +
                " where sl.temp is not null" +
                " and sl.cityRegionCodeId = :cityId" +
                " and sl.effectiveTime = :efTime" +
                " order by sl.announceTime desc" +
                " limit 1";

        return em.createQuery(sql, Integer.class)
                .setParameter("cityId", regionCode.getId())
                .setParameter("efTime", effectiveTime)
                .getSingleResult();
    }
}