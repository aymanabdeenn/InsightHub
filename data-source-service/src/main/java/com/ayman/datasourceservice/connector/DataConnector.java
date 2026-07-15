package com.ayman.datasourceservice.connector;

import com.ayman.datasourceservice.domain.ConnectorType;
import com.ayman.datasourceservice.domain.PlainConnectionConfig;
import com.ayman.datasourceservice.domain.TableMetadata;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DataConnector {
    ConnectorType getType();
    void testConnection(PlainConnectionConfig config);
    String buildJdbcUrl(PlainConnectionConfig config);
    List<TableMetadata> introspectSchema(UUID tenantId, UUID dataSourceId, PlainConnectionConfig config);
    Page<Map<String, Object>> readTable(String tenantId, String dataSourceId, PlainConnectionConfig config, String tableName, int page, int size);
}
