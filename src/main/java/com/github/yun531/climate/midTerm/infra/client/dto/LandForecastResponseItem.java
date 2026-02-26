package com.github.yun531.climate.midTerm.infra.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.yun531.climate.midTerm.domain.pop.MidPopDraft;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LandForecastResponseItem {

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

    public List<MidPopDraft> toDrafts(LocalDateTime announceTime) {
        List<MidPopDraft> list = new ArrayList<>();

        final int AM = 9;
        final int PM = 21;

        if (rnSt4Am != null) list.add(new MidPopDraft(announceTime.plusDays(4).withHour(AM), rnSt4Am));
        if (rnSt4Pm != null) list.add(new MidPopDraft(announceTime.plusDays(4).withHour(PM), rnSt4Pm));

        list.add(new MidPopDraft(announceTime.plusDays(5).withHour(AM), rnSt5Am));
        list.add(new MidPopDraft(announceTime.plusDays(5).withHour(PM), rnSt5Pm));
        list.add(new MidPopDraft(announceTime.plusDays(6).withHour(AM), rnSt6Am));
        list.add(new MidPopDraft(announceTime.plusDays(6).withHour(PM), rnSt6Pm));
        list.add(new MidPopDraft(announceTime.plusDays(7).withHour(AM), rnSt7Am));
        list.add(new MidPopDraft(announceTime.plusDays(7).withHour(PM), rnSt7Pm));

        list.add(new MidPopDraft(announceTime.plusDays(8).withHour(AM), rnSt8));
        list.add(new MidPopDraft(announceTime.plusDays(9).withHour(AM), rnSt9));
        list.add(new MidPopDraft(announceTime.plusDays(10).withHour(AM), rnSt10));

        return list;
    }
}