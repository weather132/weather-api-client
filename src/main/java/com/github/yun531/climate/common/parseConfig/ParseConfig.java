package com.github.yun531.climate.common.parseConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class ParseConfig {
    @Getter
    private final Configuration configuration;

    public ParseConfig() {
        this.configuration = Configuration.builder()
                .mappingProvider(new JacksonMappingProvider(new ObjectMapper()))
                .build();
    }
}
