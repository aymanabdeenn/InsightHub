package com.ayman.datasourceservice.domain.dto;

import com.ayman.datasourceservice.domain.ConnectorType;
import com.ayman.datasourceservice.domain.PlainConnectionConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TestConnectionRequestDTO(

        @NotNull(message = "Connector type is required")
        ConnectorType type,
        @NotNull(message = "Connection configuration is required")
        @Valid
        @JsonProperty("connectionConfig")
        PlainConnectionConfig config) {}
