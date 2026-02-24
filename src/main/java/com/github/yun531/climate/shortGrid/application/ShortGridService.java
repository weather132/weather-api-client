package com.github.yun531.climate.shortGrid.application;

import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.shortGrid.domain.ShortGrid;
import com.github.yun531.climate.shortGrid.domain.ShortGridClient;
import com.github.yun531.climate.shortGrid.domain.ShortGridRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShortGridService {
    private final ShortGridRepository shortGridRepository;
    private final ShortGridClient client;

    public ShortGridService(ShortGridRepository shortGridRepository, ShortGridClient client) {
        this.shortGridRepository = shortGridRepository;
        this.client = client;
    }

    public void updateShortGrid() {
        List<ShortGrid> shortGrids = client.requestShortGridsForHours(new AnnounceTime(LocalDateTime.now()), 26);
        shortGridRepository.saveAll(shortGrids);
    }
}
