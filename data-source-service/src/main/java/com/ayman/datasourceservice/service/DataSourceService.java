package com.ayman.datasourceservice.service;

import com.ayman.configlib.error.DataSourceNotFoundException;
import com.ayman.datasourceservice.connector.ConnectorFactory;
import com.ayman.datasourceservice.connector.DataConnector;
import com.ayman.datasourceservice.domain.*;
import com.ayman.datasourceservice.repository.DataSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DataSourceService {
    private final DataSourceRepository dataSourceRepository;
    private final ConnectorFactory connectorFactory;

    @Autowired
    public DataSourceService(DataSourceRepository dataSourceRepository, ConnectorFactory connectorFactory) {
        this.dataSourceRepository = dataSourceRepository;
        this.connectorFactory = connectorFactory;
    }

    public DataSource retrieveDataSource(UUID dataSourceId, UUID tenantId) {
        return dataSourceRepository.findByIdAndTenantId(dataSourceId, tenantId).orElseThrow(() -> new DataSourceNotFoundException("DATA_SOURCE_NOT_FOUND", "Data source with Id " + dataSourceId + " for the tenant with id " + tenantId + " wasn't found."));
    }

    public List<DataSource> retrieveAllDataSourcesForTenant(UUID tenantId) {
        return dataSourceRepository.findByTenantId(tenantId);
    }

    public DataSource registerDataSource(UUID tenantId, String name, ConnectorType type, PlainConnectionConfig plainConfig) {
        testConnection(type, plainConfig);

        ConnectionConfig connectionConfig = new ConnectionConfig(
                plainConfig.getHost(),
                plainConfig.getPort(),
                plainConfig.getDatabase(),
                plainConfig.getUsername(),
                plainConfig.getPassword()
        );

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

    public DataSource modifyDataSource(UUID dataSourceId, UUID tenantId, String name, ConnectionConfig config) {
        DataSource ds = dataSourceRepository.findByIdAndTenantId(dataSourceId, tenantId).orElseThrow(() -> new DataSourceNotFoundException("DATA_SOURCE_NOT_FOUND", "Data source with Id " + dataSourceId + " for the tenant with id " + tenantId + " wasn't found."));

        ds.setName(name);
        ds.setConnectionConfig(config);
        ds.setUpdatedAt(LocalDateTime.now());

        return dataSourceRepository.save(ds);
    }

    public void deleteDataSource(UUID dataSourceId, UUID tenantId) {
        DataSource ds = dataSourceRepository.findByIdAndTenantId(dataSourceId, tenantId).orElseThrow(() -> new DataSourceNotFoundException("DATA_SOURCE_NOT_FOUND", "Data source with Id " + dataSourceId + " for the tenant with id " + tenantId + " wasn't found."));
        dataSourceRepository.delete(ds);
    }

    public void testConnection(ConnectorType type, PlainConnectionConfig config) {
        connectorFactory.get(type).testConnection(config);
    }
}
