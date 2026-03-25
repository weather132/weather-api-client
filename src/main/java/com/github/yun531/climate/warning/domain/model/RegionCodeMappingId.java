package com.github.yun531.climate.warning.domain.model;

import java.io.Serializable;
import java.util.Objects;

public class RegionCodeMappingId implements Serializable {
    private String regionCode;
    private String warningRegionCode;

    public RegionCodeMappingId() {}

    public RegionCodeMappingId(String regionCode, String warningRegionCode) {
        this.regionCode = regionCode;
        this.warningRegionCode = warningRegionCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegionCodeMappingId other)) return false;
        return Objects.equals(regionCode, other.regionCode)
                && Objects.equals(warningRegionCode, other.warningRegionCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regionCode, warningRegionCode);
    }
}