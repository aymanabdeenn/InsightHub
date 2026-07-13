package com.ayman.datasourceservice.dto;

import com.ayman.datasourceservice.domain.ConnectionConfig;
import com.ayman.datasourceservice.domain.ConnectorType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DataSourceRegistrationDTO(String name, ConnectorType type, @JsonProperty("connectionConfig") ConnectionConfig config) {
}
