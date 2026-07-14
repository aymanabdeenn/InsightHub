package com.ayman.datasourceservice.connector;

import com.ayman.configlib.error.ConnectionTestFailedException;
import com.ayman.configlib.error.UnSupportedOperationException;
import com.ayman.datasourceservice.domain.ConnectorType;
import com.ayman.datasourceservice.domain.PlainConnectionConfig;
import com.ayman.datasourceservice.domain.TableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Component
public class MySqlConnector implements DataConnector {

    private static final Logger log = LoggerFactory.getLogger(MySqlConnector.class);
    private static final int CONNECTION_TIMEOUT_SECONDS = 5;

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
    public List<TableMetadata> introspectSchema(String dataSourceId) {
        throw new UnSupportedOperationException("Schema introspection is implemented in a later step.");
    }

    @Override
    public Page<Map<String, Object>> readTable(String dataSourceId, String tableName, int page, int size) {
        throw new UnsupportedOperationException("Table reads are implemented in a later step.");
    }

    private String buildJdbcUrl(PlainConnectionConfig config) {
        return String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                config.getHost(),
                config.getPort(),
                config.getDatabase()
        );
    }
}
