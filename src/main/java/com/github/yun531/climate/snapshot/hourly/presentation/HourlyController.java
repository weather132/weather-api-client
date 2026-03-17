package com.github.yun531.climate.snapshot.hourly.presentation;

import com.github.yun531.climate.shortGrid.domain.AnnounceTime;
import com.github.yun531.climate.snapshot.hourly.application.HourlyForecastDto;
import com.github.yun531.climate.snapshot.hourly.application.HourlyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/hourly")
public class HourlyController {
    private final HourlyService hourlyService;

    public HourlyController(HourlyService hourlyService) {
        this.hourlyService = hourlyService;
    }

    @GetMapping("/snapshot")
    public HourlyForecastDto getSnapshot(@RequestParam(value = "announceTime", required = false) String announceTime,
                                         @RequestParam(value = "regionCode") String regionCode) {

        return hourlyService.getSnapshot(getAnnounceTime(announceTime), regionCode);
    }


    private AnnounceTime getAnnounceTime(String announceTime) {
        return announceTime == null ? new AnnounceTime(LocalDateTime.now()) : parse(announceTime);
    }

    private AnnounceTime parse(String announceTime) {
        return new AnnounceTime(LocalDateTime.parse(announceTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
