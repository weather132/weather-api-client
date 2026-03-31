package com.github.yun531.climate.notification.infra.alert;

import com.github.yun531.climate.notification.domain.readmodel.WarningView;
import com.github.yun531.climate.notification.domain.readmodel.WarningViewReader;
import com.github.yun531.climate.warning.contract.IssuedWarningReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** warning contract 호출 + WarningView 변환 담당 */
@Component
@RequiredArgsConstructor
public class WarningViewReaderImpl implements WarningViewReader {

    private final IssuedWarningReader issuedWarningReader;
    private final WarningViewMapper mapper;

    @Override
    public List<WarningView> loadWarningViews(String regionId) {
        return issuedWarningReader.loadIssuedWarnings(regionId).stream()
                .map(mapper::toWarningView)
                .toList();
    }
}