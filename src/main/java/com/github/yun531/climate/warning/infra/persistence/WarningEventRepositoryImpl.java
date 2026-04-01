package com.github.yun531.climate.warning.infra.persistence;

import com.github.yun531.climate.warning.domain.model.WarningEvent;
import com.github.yun531.climate.warning.domain.repository.WarningEventRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WarningEventRepositoryImpl implements WarningEventRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JpaWarningEventRepository jpaRepository;

    public WarningEventRepositoryImpl(JdbcTemplate jdbcTemplate,
                                      JpaWarningEventRepository jpaRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void saveAll(List<WarningEvent> events) {
        if (events.isEmpty()) return;

        String sql = "INSERT INTO warning_event (warning_region_code, kind, level, prev_level, event_type, announce_time, effective_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, events, events.size(), (ps, e) -> {
            ps.setString(1, e.getWarningRegionCode());
            ps.setString(2, e.getKind().name());
            ps.setString(3, e.getLevel().name());
            ps.setString(4, e.getPrevLevel() != null ? e.getPrevLevel().name() : null);
            ps.setString(5, e.getEventType().name());
            ps.setObject(6, e.getAnnounceTime());
            ps.setObject(7, e.getEffectiveTime());
        });
    }

    @Override
    public List<WarningEvent> findLatestByWarningRegionCodes(List<String> warningRegionCodes) {
        if (warningRegionCodes.isEmpty()) return List.of();
        return jpaRepository.findLatestByWarningRegionCodes(warningRegionCodes);
    }
}