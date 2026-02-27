package com.github.yun531.climate.midTemperature.domain;

import com.github.yun531.climate.common.MidAnnounceTime;

import java.util.List;

public interface MidTemperatureClient {
    List<MidTemperatureDraft> requestMidTemperatureDrafts(String regId, MidAnnounceTime announceTime);
}