package com.ayman.datasourceservice.domain.dto;

import com.ayman.datasourceservice.domain.ConnectionConfig;

public record DataSourceModDTO(String name, ConnectionConfig config) {
}
