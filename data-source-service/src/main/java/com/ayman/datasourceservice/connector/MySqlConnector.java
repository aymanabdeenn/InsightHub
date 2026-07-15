package com.ayman.datasourceservice.connector;

import com.ayman.configlib.error.ConnectionTestFailedException;
import com.ayman.configlib.error.SchemaIntrospectionFailedException;
import com.ayman.datasourceservice.connector.pool.ConnectionPoolManager;
import com.ayman.datasourceservice.domain.*;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MySqlConnector implements DataConnector {

    private static final Logger log = LoggerFactory.getLogger(MySqlConnector.class);
    private static final int CONNECTION_TIMEOUT_SECONDS = 5;

    private final ConnectionPoolManager connectionPoolManager;

    @Autowired
    public MySqlConnector(ConnectionPoolManager connectionPoolManager) {
        this.connectionPoolManager = connectionPoolManager;
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
    public Page<Map<String, Object>> readTable(String dataSourceId, String tableName, int page, int size) {
        throw new UnsupportedOperationException("Table reads are implemented in a later step.");
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
