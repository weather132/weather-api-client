package com.github.yun531.climate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Pop {
    private final String regionId;
    private final LocalDateTime announceTime;
    private final LocalDateTime effectiveTime;
    private final Integer pop;
}
