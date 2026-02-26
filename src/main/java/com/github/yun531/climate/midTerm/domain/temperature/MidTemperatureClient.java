package com.github.yun531.climate.midTerm.domain.temperature;

import com.github.yun531.climate.midTerm.domain.MidAnnounceTime;

import java.util.List;

public interface MidTemperatureClient {
    List<MidTemperatureDraft> requestMidTemperatureDrafts(String regId, MidAnnounceTime announceTime);
}