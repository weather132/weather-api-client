package com.github.yun531.climate.notification.domain.readmodel;

import java.util.List;

/**
 * 기상특보 읽기 포트.
 * 구현체(WarningViewReaderImpl)는 notification.infra.warning에 위치한다.
 */
public interface WarningViewReader {

    List<WarningView> loadWarningViews(String regionId);
}