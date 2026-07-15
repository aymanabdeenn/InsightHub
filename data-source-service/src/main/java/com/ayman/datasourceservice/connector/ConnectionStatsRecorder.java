package com.ayman.datasourceservice.connector;

import com.ayman.datasourceservice.domain.PoolStats;

public interface ConnectionStatsRecorder {
    void recordConnectionAcquired(String tenantId, String dataSourceId, long durationMs, PoolStats poolStats);
    void recordQuery(String tenantId, String dataSourceId, String tableName, long durationMs, int rowCount, PoolStats poolStats);
    void recordConnectionFailed(String tenantId, String dataSourceId, String eventType, long durationMs, String errorMessage, PoolStats poolStats);
}
