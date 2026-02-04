package com.github.yun531.climate.repository;

import com.github.yun531.climate.entity.ShortGrid;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Testcontainers
@Import(ShortGridBatchRepository.class)
public class PerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(PerformanceTest.class);

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("climate")
            .withUsername("test")
            .withPassword("test")
            .withInitScripts("climate_ddl.sql", "climate_dml.sql");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private ShortGridRepository sgRepo;

    @Autowired
    private ShortGridBatchRepository sgBatchRepo;

    @Test
    void saveAllPerformance() {
        // given
        LocalDateTime time = LocalDateTime.parse("2026-01-01T00:00:00");
        List<ShortGrid> shortGrids1 = IntStream.range(1, 4000)
                .mapToObj(i -> new ShortGrid(null, time, time, i, i, i, i))
                .toList();

        List<ShortGrid> shortGrids2 = new ArrayList<>();
        for (ShortGrid sg : shortGrids1) {
            shortGrids2.add(new ShortGrid(sg.getId(), sg.getAnnounceTime(), sg.getEffectiveTime().plusHours(1), sg.getX(), sg.getY(), sg.getPop(), sg.getTemp()));
        }

        // when
        long start = System.currentTimeMillis();
        sgRepo.saveAll(shortGrids1);
        long end = System.currentTimeMillis();

        long simpleSaveAllTime = end - start;


        start = System.currentTimeMillis();
        sgBatchRepo.saveAll(shortGrids2);
        end = System.currentTimeMillis();

        long batchSaveAllTime = end - start;

        // then
        log.info("Simple saveAll : {} ms", simpleSaveAllTime);
        log.info("Batch saveAll : {} ms", batchSaveAllTime);
    }
}
