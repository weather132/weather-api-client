package com.github.yun531.climate.midLand.infra;

import com.github.yun531.climate.midLand.domain.MidLand;
import com.github.yun531.climate.midLand.domain.MidLandRepository;
import com.github.yun531.climate.provinceRegionCode.ProvinceRegionCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class MidLandRepositoryImpl implements MidLandRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JpaMidLandRepository jpaMidLandRepository;

    public MidLandRepositoryImpl(JdbcTemplate jdbcTemplate, JpaMidLandRepository jpaMidLandRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.jpaMidLandRepository = jpaMidLandRepository;
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

    @Override
    public MidLand findById(Long id) {
        return jpaMidLandRepository.findById(id).orElseThrow();
    }

    @Override
    public List<MidLand> findAll() {
        return jpaMidLandRepository.findAll();
    }

    @Override
    public MidLand findRecent(ProvinceRegionCode regionCode, LocalDateTime effectiveTime) {
        return findRecentAll(regionCode, List.of(effectiveTime))
                .getOrDefault(effectiveTime, new MidLand(null, null, null, null));
    }

    @Override
    public Map<LocalDateTime, MidLand> findRecentAll(
            ProvinceRegionCode regionCode, List<LocalDateTime> effectiveTimes
    ) {
        if (effectiveTimes == null || effectiveTimes.isEmpty()) return Map.of();

        return jpaMidLandRepository
                .findRecentByRegionAndEffectiveTimes(regionCode.getId(), effectiveTimes)
                .stream()
                .collect(Collectors.toMap(MidLand::getEffectiveTime, Function.identity()));
    }
}