package com.ayman.datasourceservice.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "data_sources")
public class DataSource {
    @Id
    private UUID id;
    private UUID tenantId;
    private String name;
    private ConnectorType type;
    private ConnectionConfig connectionConfig;
    private List<String> selectedTables;
    private DataSourceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DataSource() {}

    public DataSource(UUID tenantId, String name, ConnectorType type, ConnectionConfig connectionConfig, List<String> selectedTables, DataSourceStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.tenantId = tenantId;
        this.name = name;
        this.type = type;
        this.connectionConfig = connectionConfig;
        this.selectedTables = selectedTables;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConnectorType getType() {
        return type;
    }

    public void setType(ConnectorType type) {
        this.type = type;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public List<String> getSelectedTables() {
        return selectedTables;
    }

    public void setSelectedTables(List<String> selectedTables) {
        this.selectedTables = selectedTables;
    }

    public DataSourceStatus getStatus() {
        return status;
    }

    public void setStatus(DataSourceStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
