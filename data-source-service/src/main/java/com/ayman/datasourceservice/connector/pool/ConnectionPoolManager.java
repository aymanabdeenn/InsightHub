package com.ayman.datasourceservice.connector.pool;

import com.ayman.datasourceservice.connector.ConnectorFactory;
import com.ayman.datasourceservice.domain.ConnectorType;
import com.ayman.datasourceservice.domain.PlainConnectionConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class ConnectionPoolManager {

    private static final Logger log = LoggerFactory.getLogger(ConnectionPoolManager.class);

    private final ConnectorFactory connectorFactory;
    private final Map<String, HikariDataSource> pools = new ConcurrentHashMap<>();

    @Autowired
    public ConnectionPoolManager(@Lazy ConnectorFactory connectorFactory) {
        this.connectorFactory = connectorFactory;
    }

    public HikariDataSource getPool(String tenantId, String dataSourceId, ConnectorType type, PlainConnectionConfig config) {
        String key = poolKey(tenantId, dataSourceId);
        return pools.computeIfAbsent(key, k -> createPool(tenantId, dataSourceId, type, config));
    }

    public void closePool(String tenantId, String dataSourceId) {
        String key = poolKey(tenantId, dataSourceId);
        HikariDataSource pool = pools.remove(key);
        if (pool != null) {
            log.info("Closing connection pool: {}", key);
            pool.close();
        }
    }

    private HikariDataSource createPool(String tenantId, String dataSourceId, ConnectorType type, PlainConnectionConfig config) {
        log.info("Creating new connection pool: {}:{}", tenantId, dataSourceId);

        HikariConfig hikariConfig = new HikariConfig();

        String jdbcUrl = connectorFactory.get(type).buildJdbcUrl(config);

        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());

        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setIdleTimeout(TimeUnit.MINUTES.toMillis(5));
        hikariConfig.setConnectionTimeout(TimeUnit.SECONDS.toMillis(10));
        hikariConfig.setPoolName(String.format("pool-%s-%s", tenantId, dataSourceId));

        return new HikariDataSource(hikariConfig);
    }

    private String poolKey(String tenantId, String dataSourceId) {
        return tenantId + ":" + dataSourceId;
    }
}
