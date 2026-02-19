package com.github.yun531.climate.shortLand.domain;

import java.util.List;

public interface ShortLandRepository {
    ShortLand save(ShortLand shortLand);
    void saveAll(List<ShortLand> shortLands);
}
