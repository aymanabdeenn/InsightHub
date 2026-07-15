package com.ayman.datasourceservice.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "dss_connection_stats")
public class ConnectionStats {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID dataSourceId;
    private String eventType; // CONNECTION_ACQUIRED, QUERY, CONNECTION_FAILED
    private String tableName; // null for connection events
    private long durationMs;
    private Integer rowCount; // null when not applicable
    private String status; // SUCCESS or FAILED
    private String errorMessage;
    private PoolStats poolStats;
    private LocalDateTime timestamp;

    public ConnectionStats() {
    }

    public ConnectionStats(UUID tenantId, UUID dataSourceId, String eventType, String tableName,
                           long durationMs, Integer rowCount, String status, String errorMessage,
                           PoolStats poolStats, LocalDateTime timestamp) {
        this.tenantId = tenantId;
        this.dataSourceId = dataSourceId;
        this.eventType = eventType;
        this.tableName = tableName;
        this.durationMs = durationMs;
        this.rowCount = rowCount;
        this.status = status;
        this.errorMessage = errorMessage;
        this.poolStats = poolStats;
        this.timestamp = timestamp;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getDataSourceId() { return dataSourceId; }
    public String getEventType() { return eventType; }
    public String getTableName() { return tableName; }
    public long getDurationMs() { return durationMs; }
    public Integer getRowCount() { return rowCount; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public PoolStats getPoolStats() { return poolStats; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
