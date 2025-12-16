package com.github.yun531.climate.dto;

import com.github.yun531.climate.entity.MidPop;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Pop {
    private final String regionCode;
    private final LocalDateTime announceTime;
    private final LocalDateTime effectiveTime;
    private final Integer pop;

    public MidPop toMidPopEntity() {
        return new MidPop(announceTime, effectiveTime, regionCode, pop);
    }
}
