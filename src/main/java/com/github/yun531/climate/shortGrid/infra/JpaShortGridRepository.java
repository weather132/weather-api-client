package com.github.yun531.climate.shortGrid.infra;

import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaShortGridRepository extends JpaRepository<ShortGrid, Long> {
    List<ShortGrid> findByAnnounceTimeAndXAndY(AnnounceTime announceTime, Integer x, Integer y);

    @Query(value = """
            SELECT sg.* FROM short_grid sg
            INNER JOIN (
                SELECT MAX(announce_time) AS max_at
                FROM short_grid
                WHERE x = :x AND y = :y
            ) latest ON sg.announce_time = latest.max_at
            WHERE sg.x = :x AND sg.y = :y
            """, nativeQuery = true)
    List<ShortGrid> findRecentByXAndY(
            @Param("x") int x,
            @Param("y") int y
    );
}
