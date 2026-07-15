package com.ayman.datasourceservice.domain.dto;

import com.ayman.datasourceservice.domain.PlainConnectionConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DataSourceModDTO(String name, @JsonProperty("connectionConfig") PlainConnectionConfig config) {
}
