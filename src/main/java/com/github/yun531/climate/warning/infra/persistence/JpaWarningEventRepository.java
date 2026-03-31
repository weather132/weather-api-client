package com.github.yun531.climate.warning.infra.persistence;

import com.github.yun531.climate.warning.domain.model.WarningEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaWarningEventRepository extends JpaRepository<WarningEvent, Long> {

    @Query(value = """
            SELECT e.* FROM warning_event e
            INNER JOIN (
                SELECT warning_region_code, kind, MAX(id) AS max_id
                FROM warning_event
                WHERE warning_region_code IN :codes
                GROUP BY warning_region_code, kind
            ) latest ON e.id = latest.max_id
            """, nativeQuery = true)
    List<WarningEvent> findLatestByWarningRegionCodes(@Param("codes") List<String> warningRegionCodes);
}