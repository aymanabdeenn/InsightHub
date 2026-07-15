package com.ayman.datasourceservice.connector.sql;

import com.ayman.configlib.error.ConnectionTestFailedException;
import com.ayman.configlib.error.SchemaIntrospectionFailedException;
import com.ayman.datasourceservice.connector.ConnectionStatsRecorder;
import com.ayman.datasourceservice.connector.DataConnector;
import com.ayman.datasourceservice.connector.pool.ConnectionPoolManager;
import com.ayman.datasourceservice.domain.*;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MySqlConnector implements DataConnector {

    private static final Logger log = LoggerFactory.getLogger(MySqlConnector.class);
    private static final int CONNECTION_TIMEOUT_SECONDS = 5;

    private final ConnectionPoolManager connectionPoolManager;
    private final ConnectionStatsRecorder statsRecorder;

    @Autowired
    public MySqlConnector(ConnectionPoolManager connectionPoolManager, ConnectionStatsRecorder statsRecorder) {
        this.connectionPoolManager = connectionPoolManager;
        this.statsRecorder = statsRecorder;
    }

    @Override
    public ConnectorType getType() {
        return ConnectorType.MYSQL;
    }

    @Override
    public void testConnection(PlainConnectionConfig config) {
        String jdbcUrl = buildJdbcUrl(config);

        log.info("Testing MySQL connection: host={}, database={}", config.getHost(), config.getDatabase());

        try {
            DriverManager.setLoginTimeout(CONNECTION_TIMEOUT_SECONDS);
            try (Connection connection = DriverManager.getConnection(jdbcUrl, config.getUsername(), config.getPassword())) {
                // Connection succeeded — nothing more to do. try-with-resources closes it immediately.
                log.info("Connection has been established.");
            }
        } catch (SQLException e) {
            log.warn("MySQL connection test failed: host={}, database={}, reason={}",
                    config.getHost(), config.getDatabase(), e.getMessage());
            throw new ConnectionTestFailedException(
                    "CONNECTION_TEST_FAILED",
                    "Could not connect to the database with the provided credentials."
            );
        }
    }

    @Override
    public List<TableMetadata> introspectSchema(UUID tenantId, UUID dataSourceId, PlainConnectionConfig config) {
        HikariDataSource pool = connectionPoolManager.getPool(tenantId.toString(), dataSourceId.toString(), getType(), config);

        String sql = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, IS_NULLABLE " +
                "FROM information_schema.columns " +
                "WHERE TABLE_SCHEMA = ? " +
                "ORDER BY TABLE_NAME, ORDINAL_POSITION";

        Map<String, List<ColumnMetadata>> columnsByTable = new LinkedHashMap<>();

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, config.getDatabase());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    ColumnMetadata column = new ColumnMetadata(
                            rs.getString("COLUMN_NAME"),
                            rs.getString("DATA_TYPE"),
                            "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE"))
                    );
                    columnsByTable.computeIfAbsent(tableName, k -> new ArrayList<>()).add(column);
                }
            }
        } catch (SQLException e) {
            log.warn("Schema introspection failed: dataSourceId={}, reason={}", dataSourceId, e.getMessage());
            throw new SchemaIntrospectionFailedException("SCHEMA_INTROSPECTION_FAILED",
                    "Could not introspect schema for this data source.");
        }

        return columnsByTable.entrySet().stream()
                .map(entry -> new TableMetadata(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public Page<Map<String, Object>> readTable(String tenantId, String dataSourceId, PlainConnectionConfig config,
                                               String tableName, int page, int size) {
        HikariDataSource pool = connectionPoolManager.getPool(tenantId, dataSourceId, getType(), config);

        long acquireStart = System.currentTimeMillis();
        Connection conn;
        try {
            conn = pool.getConnection();
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - acquireStart;
            statsRecorder.recordConnectionFailed(tenantId, dataSourceId, "CONNECTION_ACQUIRED", duration,
                    e.getMessage(), connectionPoolManager.getPoolStats(tenantId, dataSourceId));
            throw new ConnectionTestFailedException("CONNECTION_ACQUISITION_FAILED", "Could not acquire a database connection.");
        }
        long acquireDuration = System.currentTimeMillis() - acquireStart;
        statsRecorder.recordConnectionAcquired(tenantId, dataSourceId, acquireDuration,
                connectionPoolManager.getPoolStats(tenantId, dataSourceId));

        long queryStart = System.currentTimeMillis();
        try (conn) {
            String sql = "SELECT * FROM " + tableName + " LIMIT ? OFFSET ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, size);
                stmt.setInt(2, page * size);

                List<Map<String, Object>> rows = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(meta.getColumnLabel(i), rs.getObject(i));
                        }
                        rows.add(row);
                    }
                }

                long queryDuration = System.currentTimeMillis() - queryStart;
                statsRecorder.recordQuery(tenantId, dataSourceId, tableName, queryDuration, rows.size(),
                        connectionPoolManager.getPoolStats(tenantId, dataSourceId));

                return new PageImpl<>(rows, PageRequest.of(page, size), rows.size());
            }
        } catch (SQLException e) {
            long queryDuration = System.currentTimeMillis() - queryStart;
            statsRecorder.recordConnectionFailed(tenantId, dataSourceId, "QUERY", queryDuration,
                    e.getMessage(), connectionPoolManager.getPoolStats(tenantId, dataSourceId));
            log.warn("Table read failed: dataSourceId={}, table={}, reason={}", dataSourceId, tableName, e.getMessage());
            throw new SchemaIntrospectionFailedException("TABLE_READ_FAILED", "Could not read data from the requested table.");
        }
    }

    @Override
    public String buildJdbcUrl(PlainConnectionConfig config) {
        return String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                config.getHost(),
                config.getPort(),
                config.getDatabase()
        );
    }
}
