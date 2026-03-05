package com.github.yun531.climate.midLand.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.midLand.domain.MidLand;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MidLandItem {
    @Getter
    private String regId;
    private Integer rnSt4Am;
    private Integer rnSt4Pm;
    private Integer rnSt5Am;
    private Integer rnSt5Pm;
    private Integer rnSt6Am;
    private Integer rnSt6Pm;
    private Integer rnSt7Am;
    private Integer rnSt7Pm;
    private Integer rnSt8;
    private Integer rnSt9;
    private Integer rnSt10;


    public List<MidLand> toMidLands(MidAnnounceTime announceTime, Long regionCodeId) {
        List<MidLand> midLands = new ArrayList<>();

        final int AM = 9;
        final int PM = 21;

        LocalDateTime time = announceTime.getTime();

        if (rnSt4Am != null) {
            midLands.add(new MidLand(announceTime, time.plusDays(4).withHour(AM), regionCodeId, rnSt4Am));
        }
        if (rnSt4Pm != null) {
            midLands.add(new MidLand(announceTime, time.plusDays(4).withHour(PM), regionCodeId, rnSt4Pm));
        }

        midLands.add(new MidLand(announceTime, time.plusDays(5).withHour(AM), regionCodeId, rnSt5Am));
        midLands.add(new MidLand(announceTime, time.plusDays(5).withHour(PM), regionCodeId, rnSt5Pm));
        midLands.add(new MidLand(announceTime, time.plusDays(6).withHour(AM), regionCodeId, rnSt6Am));
        midLands.add(new MidLand(announceTime, time.plusDays(6).withHour(PM), regionCodeId, rnSt6Pm));
        midLands.add(new MidLand(announceTime, time.plusDays(7).withHour(AM), regionCodeId, rnSt7Am));
        midLands.add(new MidLand(announceTime, time.plusDays(7).withHour(PM), regionCodeId, rnSt7Pm));
        midLands.add(new MidLand(announceTime, time.plusDays(8).withHour(AM), regionCodeId, rnSt8));
        midLands.add(new MidLand(announceTime, time.plusDays(9).withHour(AM), regionCodeId, rnSt9));
        midLands.add(new MidLand(announceTime, time.plusDays(10).withHour(AM), regionCodeId, rnSt10));

        return midLands;
    }


    protected MidLandItem() {}
}
