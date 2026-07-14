package com.ayman.datasourceservice.domain.dto;

import com.ayman.datasourceservice.domain.ConnectorType;
import com.ayman.datasourceservice.domain.PlainConnectionConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TestConnectionRequestDTO(ConnectorType type, @JsonProperty("connectionConfig") PlainConnectionConfig config) {
}
