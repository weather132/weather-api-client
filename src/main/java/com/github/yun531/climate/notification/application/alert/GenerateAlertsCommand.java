package com.github.yun531.climate.notification.application.alert;

import com.github.yun531.climate.notification.domain.model.AlertTypeEnum;
import org.springframework.lang.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record GenerateAlertsCommand(
        List<String> regionIds,
        Set<AlertTypeEnum> enabledTypes,
        @Nullable Set<String> warningKinds,
        @Nullable Integer withinHours
) {
    public GenerateAlertsCommand {
        enabledTypes = (enabledTypes == null || enabledTypes.isEmpty())
                ? EnumSet.noneOf(AlertTypeEnum.class)
                : EnumSet.copyOf(enabledTypes);

        warningKinds = (warningKinds != null && !warningKinds.isEmpty())
                ? Set.copyOf(warningKinds)
                : null;
    }

    public boolean hasNoTypes() {
        return enabledTypes.isEmpty();
    }

    public boolean isEnabled(AlertTypeEnum type) {
        return enabledTypes.contains(type);
    }
}