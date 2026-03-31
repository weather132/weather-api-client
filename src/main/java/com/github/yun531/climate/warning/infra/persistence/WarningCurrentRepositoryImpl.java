package com.github.yun531.climate.warning.infra.persistence;

import com.github.yun531.climate.warning.domain.model.WarningCurrent;
import com.github.yun531.climate.warning.domain.repository.WarningCurrentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class WarningCurrentRepositoryImpl implements WarningCurrentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JpaWarningCurrentRepository jpaRepository;

    public WarningCurrentRepositoryImpl(JdbcTemplate jdbcTemplate,
                                        JpaWarningCurrentRepository jpaRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void replaceAll(List<WarningCurrent> currents) {
        jdbcTemplate.execute("TRUNCATE TABLE warning_current");

        if (currents.isEmpty()) return;

        String sql = "INSERT INTO warning_current (warning_region_code, kind, level, announce_time, effective_time) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, currents, currents.size(), (ps, c) -> {
            ps.setString(1, c.getWarningRegionCode());
            ps.setString(2, c.getKind().name());
            ps.setString(3, c.getLevel().name());
            ps.setObject(4, c.getAnnounceTime());
            ps.setObject(5, c.getEffectiveTime());
        });
    }

    @Override
    public List<WarningCurrent> findAll() {
        return jpaRepository.findAll();
    }
}