package com.github.yun531.climate.notification.domain.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * REST 에서도 payload를 "타입" 그대로 내보내기 위한 다형성 payload 루트.
 * JSON 예시:
 *  "payload": { "payloadType":"RAIN_ONSET", "source":"...", ... }
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "payloadType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RainOnsetPayload.class, name = "RAIN_ONSET"),
        @JsonSubTypes.Type(value = RainForecastPayload.class, name = "RAIN_FORECAST")
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface AlertPayload
        permits RainOnsetPayload, RainForecastPayload {
}