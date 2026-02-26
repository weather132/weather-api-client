package com.github.yun531.climate.midTerm.domain.pop;

import com.github.yun531.climate.midTerm.domain.MidAnnounceTime;

import java.util.List;

public interface MidPopClient {
    List<MidPopDraft> requestMidPopDrafts(String regId, MidAnnounceTime announceTime);
}