package com.github.yun531.climate.warning.domain.model;

import java.io.Serializable;
import java.util.Objects;

public class WarningId implements Serializable {
    private String warningRegionCode;
    private WarningKind kind;

    public WarningId() {}

    public WarningId(String warningRegionCode, WarningKind kind) {
        this.warningRegionCode = warningRegionCode;
        this.kind = kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WarningId other)) return false;
        return Objects.equals(warningRegionCode, other.warningRegionCode)
                && kind == other.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(warningRegionCode, kind);
    }
}