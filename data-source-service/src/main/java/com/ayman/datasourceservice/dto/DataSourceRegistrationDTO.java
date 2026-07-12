package com.ayman.datasourceservice.dto;

import com.ayman.datasourceservice.domain.ConnectionConfig;
import com.ayman.datasourceservice.domain.ConnectorType;

public record DataSourceRegistrationDTO(String name, ConnectorType type, ConnectionConfig config) {
}
