package com.github.yun531.climate.snapshot.infra.reader;

import com.github.yun531.climate.snapshot.domain.model.SnapKind;

public record SnapshotKey(String regionId, SnapKind kind) {

    private static final String SEP = ":";

    public SnapshotKey {
        if (regionId == null || regionId.isBlank()) {
            throw new IllegalArgumentException("regionId must not be blank");
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null");
        }
    }

    public int asSnapId() {
        return toCode(kind);
    }

    public String asCacheKey() {
        return regionId + SEP + asSnapId();
    }

    public static SnapshotKey of(String regionId, SnapKind kind) {
        return new SnapshotKey(regionId, kind);
    }

    private static int toCode(SnapKind kind) {
        return switch (kind) {
            case CURRENT -> 1;
            case PREVIOUS -> 10;
        };
    }
}