package com.ayman.datasourceservice.service;

import com.ayman.datasourceservice.connector.ConnectionStatsRecorder;
import com.ayman.datasourceservice.domain.ConnectionStats;
import com.ayman.datasourceservice.domain.PoolStats;
import com.ayman.datasourceservice.repository.ConnectionStatsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ConnectionStatsService implements ConnectionStatsRecorder {

    private final ConnectionStatsRepository repository;

    public ConnectionStatsService(ConnectionStatsRepository repository) {
        this.repository = repository;
    }

    @Override
    public void recordConnectionAcquired(String tenantId, String dataSourceId, long durationMs, PoolStats poolStats) {
        save("CONNECTION_ACQUIRED", tenantId, dataSourceId, null, durationMs, null, "SUCCESS", null, poolStats);
    }

    @Override
    public void recordQuery(String tenantId, String dataSourceId, String tableName, long durationMs, int rowCount, PoolStats poolStats) {
        save("QUERY", tenantId, dataSourceId, tableName, durationMs, rowCount, "SUCCESS", null, poolStats);
    }

    @Override
    public void recordConnectionFailed(String tenantId, String dataSourceId, String eventType, long durationMs, String errorMessage, PoolStats poolStats) {
        save(eventType, tenantId, dataSourceId, null, durationMs, null, "FAILED", errorMessage, poolStats);
    }

    private void save(String eventType, String tenantId, String dataSourceId, String tableName,
                      long durationMs, Integer rowCount, String status, String errorMessage, PoolStats poolStats) {
        ConnectionStats stats = new ConnectionStats(
                UUID.fromString(tenantId), UUID.fromString(dataSourceId), eventType, tableName,
                durationMs, rowCount, status, errorMessage, poolStats, LocalDateTime.now()
        );
        stats.setId(UUID.randomUUID());
        repository.save(stats);
    }
}
