package com.github.yun531.climate.midTemperature.domain;

import java.util.List;

public interface MidTemperatureRepository {
    void saveAll(List<MidTemperature> midTemps);
}