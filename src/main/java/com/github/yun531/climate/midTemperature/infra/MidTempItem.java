package com.github.yun531.climate.midTemperature.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.yun531.climate.common.MidAnnounceTime;
import com.github.yun531.climate.midTemperature.domain.MidTemperature;
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
public class MidTempItem {

    private String regId;

    private Integer taMin4;
    private Integer taMax4;
    private Integer taMin5;
    private Integer taMax5;
    private Integer taMin6;
    private Integer taMax6;
    private Integer taMin7;
    private Integer taMax7;
    private Integer taMin8;
    private Integer taMax8;
    private Integer taMin9;
    private Integer taMax9;
    private Integer taMin10;
    private Integer taMax10;


    public List<MidTemperature> toMidTemperatures(MidAnnounceTime announceTime, Long regionId) {
        List<MidTemperature> list = new ArrayList<>();

        final LocalDateTime standardEfTime = announceTime.getTime().withHour(9);

        if (taMin4 != null && taMax4 != null) {
            list.add(new MidTemperature(
                    announceTime,
                    standardEfTime.plusDays(4),
                    regionId,
                    taMax4,
                    taMin4
            ));
        }

        list.add(new MidTemperature(announceTime, standardEfTime.plusDays(5), regionId, taMax5, taMin5));
        list.add(new MidTemperature(announceTime, standardEfTime.plusDays(6), regionId, taMax6, taMin6));
        list.add(new MidTemperature(announceTime, standardEfTime.plusDays(7), regionId, taMax7, taMin7));
        list.add(new MidTemperature(announceTime, standardEfTime.plusDays(8), regionId, taMax8, taMin8));
        list.add(new MidTemperature(announceTime, standardEfTime.plusDays(9), regionId, taMax9, taMin9));
        list.add(new MidTemperature(announceTime, standardEfTime.plusDays(10), regionId, taMax10, taMin10));

        return list;
    }
}