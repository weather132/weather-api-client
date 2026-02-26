package com.github.yun531.climate.midTerm.domain.temperature;

import java.util.List;

public interface MidTemperatureRepository {
    void saveAll(List<MidTemperature> midTemps);
}