package com.ayman.datasourceservice.domain.dto;

import com.ayman.datasourceservice.domain.ConnectorType;
import com.ayman.datasourceservice.domain.PlainConnectionConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DataSourceRegistrationDTO(String name, ConnectorType type, @JsonProperty("connectionConfig") PlainConnectionConfig config) {
}
