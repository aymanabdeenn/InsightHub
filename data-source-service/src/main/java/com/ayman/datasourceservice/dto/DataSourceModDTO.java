package com.ayman.datasourceservice.dto;

import com.ayman.datasourceservice.domain.ConnectionConfig;

public record DataSourceModDTO(String name, ConnectionConfig config) {
}
