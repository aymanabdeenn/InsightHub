package com.ayman.datasourceservice.domain.dto;

import com.ayman.datasourceservice.domain.ConnectorType;
import com.ayman.datasourceservice.domain.PlainConnectionConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DataSourceRegistrationDTO(
        @NotBlank(message = "Data source name cannot be blank")
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
        String name,
        ConnectorType type,
        @NotNull(message = "Connection configuration is required")
        @Valid
        @JsonProperty("connectionConfig") PlainConnectionConfig config) {

}
