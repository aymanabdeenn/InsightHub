package com.ayman.datasourceservice.domain.dto;

import com.ayman.datasourceservice.domain.ConnectorType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DataSourceResponseDTO(UUID id, UUID tenantId, String name, ConnectorType type, ConnectionConfigResponseDTO config, List<String> selectedTables, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
