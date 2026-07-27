package com.ayman.datasourceservice.service;

import com.ayman.configlib.error.DataSourceNotFoundException;
import com.ayman.configlib.error.DuplicateDataSourceException;
import com.ayman.configlib.error.TableNotSelectedException;
import com.ayman.datasourceservice.connector.ConnectorFactory;
import com.ayman.datasourceservice.connector.pool.ConnectionPoolManager;
import com.ayman.datasourceservice.domain.*;
import com.ayman.datasourceservice.repository.DataSourceRepository;
import com.ayman.datasourceservice.security.CredentialEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DataSourceService {
    private final DataSourceRepository dataSourceRepository;
    private final ConnectorFactory connectorFactory;
    private final CredentialEncryptionService credentialEncryptionService;
    private final ConnectionPoolManager connectionPoolManager;

    @Autowired
    public DataSourceService(DataSourceRepository dataSourceRepository, ConnectorFactory connectorFactory, CredentialEncryptionService  credentialEncryptionService, ConnectionPoolManager connectionPoolManager) {
        this.dataSourceRepository = dataSourceRepository;
        this.connectorFactory = connectorFactory;
        this.credentialEncryptionService = credentialEncryptionService;
        this.connectionPoolManager = connectionPoolManager;
    }

    public DataSource retrieveDataSource(UUID dataSourceId, UUID tenantId) {
        return dataSourceRepository.findByIdAndTenantId(dataSourceId, tenantId).orElseThrow(() -> new DataSourceNotFoundException("DATA_SOURCE_NOT_FOUND", "Data source with Id " + dataSourceId + " for the tenant with id " + tenantId + " wasn't found."));
    }

    public List<DataSource> retrieveAllDataSourcesForTenant(UUID tenantId) {
        return dataSourceRepository.findByTenantId(tenantId);
    }

    public DataSource registerDataSource(UUID tenantId, String name, ConnectorType type, PlainConnectionConfig plainConfig) {
        if(dataSourceRepository.existsByTenantIdAndName(tenantId, name)) throw new DuplicateDataSourceException("DUPLICATE_DATA_SOURCE", "A data source with tenant Id:" + tenantId + " and name: " + name + " already exists.");
        testConnection(type, plainConfig);

        ConnectionConfig connectionConfig = credentialEncryptionService.encrypt(plainConfig);

        DataSource ds = new DataSource(
                tenantId,
                name,
                type,
                connectionConfig,
                new ArrayList<>(),
                DataSourceStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        ds.setId(UUID.randomUUID());
        return dataSourceRepository.save(ds);
    }

    public DataSource modifyDataSource(UUID dataSourceId, UUID tenantId, String name, PlainConnectionConfig config) {
        DataSource ds = dataSourceRepository.findByIdAndTenantId(dataSourceId, tenantId).orElseThrow(() -> new DataSourceNotFoundException("DATA_SOURCE_NOT_FOUND", "Data source with Id " + dataSourceId + " for the tenant with id " + tenantId + " wasn't found."));
        if(dataSourceRepository.existsByTenantIdAndName(tenantId, name)) throw new DuplicateDataSourceException("DUPLICATE_DATA_SOURCE", "A data source with tenant Id:" + tenantId + " and name: " + name + " already exists.");

        testConnection(ds.getType(), config);

        ds.setName(name);
        ds.setConnectionConfig(credentialEncryptionService.encrypt(config));
        ds.setUpdatedAt(LocalDateTime.now());

        DataSource saved = dataSourceRepository.save(ds);
        connectionPoolManager.closePool(tenantId.toString(), dataSourceId.toString());
        return saved;
    }

    public void deleteDataSource(UUID dataSourceId, UUID tenantId) {
        DataSource ds = dataSourceRepository.findByIdAndTenantId(dataSourceId, tenantId).orElseThrow(() -> new DataSourceNotFoundException("DATA_SOURCE_NOT_FOUND", "Data source with Id " + dataSourceId + " for the tenant with id " + tenantId + " wasn't found."));
        dataSourceRepository.delete(ds);
        connectionPoolManager.closePool(tenantId.toString(), dataSourceId.toString());
    }

    public void testConnection(ConnectorType type, PlainConnectionConfig config) {
        connectorFactory.get(type).testConnection(config);
    }

    public List<TableMetadata> getSchema(UUID dataSourceId, UUID tenantId) {
        DataSource ds = dataSourceRepository.findByIdAndTenantId(dataSourceId, tenantId).orElseThrow(() -> new DataSourceNotFoundException("DATA_SOURCE_NOT_FOUND", "Data source with Id " + dataSourceId + " for the tenant with id " + tenantId + " wasn't found."));

        PlainConnectionConfig config = credentialEncryptionService.decrypt(ds.getConnectionConfig());

        return connectorFactory.get(ds.getType()).introspectSchema(tenantId, dataSourceId, config);
    }

    public DataSource updateSelectedTables(UUID dataSourceId, UUID tenantId, List<String> selectedTables) {
        DataSource ds = dataSourceRepository.findByIdAndTenantId(dataSourceId, tenantId).orElseThrow(() -> new DataSourceNotFoundException("DATA_SOURCE_NOT_FOUND", "Data source with Id " + dataSourceId + " for the tenant with id " + tenantId + " wasn't found."));

        ds.setSelectedTables(selectedTables);
        ds.setUpdatedAt(LocalDateTime.now());
        return dataSourceRepository.save(ds);
    }

    public Page<Map<String, Object>> readTable(UUID dataSourceId, UUID tenantId, String tableName, int page, int size) {
        DataSource ds = dataSourceRepository.findByIdAndTenantId(dataSourceId, tenantId)
                .orElseThrow(() -> new DataSourceNotFoundException("DATA_SOURCE_NOT_FOUND", "..."));

        if (!ds.getSelectedTables().contains(tableName)) {
            throw new TableNotSelectedException("TABLE_NOT_SELECTED",
                    "Table '" + tableName + "' is not selected for querying on this data source.");
        }

        PlainConnectionConfig plainConfig = credentialEncryptionService.decrypt(ds.getConnectionConfig());

        return connectorFactory.get(ds.getType())
                .readTable(tenantId.toString(), dataSourceId.toString(), plainConfig, tableName, page, size);
    }
}
