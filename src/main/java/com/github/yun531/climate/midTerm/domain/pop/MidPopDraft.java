package com.github.yun531.climate.midTerm.domain.pop;

import java.time.LocalDateTime;

public record MidPopDraft(
        LocalDateTime effectiveTime,
        Integer pop
) {}