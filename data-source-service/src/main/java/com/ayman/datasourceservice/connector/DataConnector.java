package com.ayman.datasourceservice.connector;

import com.ayman.datasourceservice.domain.ConnectorType;
import com.ayman.datasourceservice.domain.PlainConnectionConfig;
import com.ayman.datasourceservice.domain.TableMetadata;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DataConnector {
    ConnectorType getType();
    void testConnection(PlainConnectionConfig config);
    String buildJdbcUrl(PlainConnectionConfig config);
    List<TableMetadata> introspectSchema(String dataSourceId);
    Page<Map<String, Object>> readTable(String dataSourceId, String tableName, int page, int size);
}
