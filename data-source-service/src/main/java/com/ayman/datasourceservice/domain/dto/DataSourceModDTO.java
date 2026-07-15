package com.ayman.datasourceservice.domain.dto;

import com.ayman.datasourceservice.domain.PlainConnectionConfig;

public record DataSourceModDTO(String name, PlainConnectionConfig config) {
}
