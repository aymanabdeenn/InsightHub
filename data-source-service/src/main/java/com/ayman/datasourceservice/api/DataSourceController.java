package com.ayman.datasourceservice.api;

import com.ayman.datasourceservice.domain.DataSource;
import com.ayman.datasourceservice.domain.TableMetadata;
import com.ayman.datasourceservice.domain.dto.DataSourceModDTO;
import com.ayman.datasourceservice.domain.dto.DataSourceRegistrationDTO;
import com.ayman.datasourceservice.domain.dto.SelectedTablesDTO;
import com.ayman.datasourceservice.domain.dto.TestConnectionRequestDTO;
import com.ayman.datasourceservice.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/data-sources")
public class DataSourceController {
    private final DataSourceService dataSourceService;

    @Autowired
    public DataSourceController(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataSource> getDataSource(@PathVariable UUID id, @RequestHeader("X-Tenant-Id") UUID tenantId) {
        return ResponseEntity.ok(dataSourceService.retrieveDataSource(id, tenantId));
    }

    @GetMapping
    public ResponseEntity<List<DataSource>> getAllDataSources(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return ResponseEntity.ok(dataSourceService.retrieveAllDataSourcesForTenant(tenantId));
    }

    @PostMapping
    public ResponseEntity<DataSource> registerDataSource(@RequestHeader("X-Tenant-Id") UUID tenantId, @RequestBody DataSourceRegistrationDTO dto) {
        DataSource ds = dataSourceService.registerDataSource(tenantId, dto.name(), dto.type(), dto.config());
        return ResponseEntity.status(HttpStatus.CREATED).body(ds);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DataSource> updateDataSource(@PathVariable UUID id, @RequestHeader("X-Tenant-Id") UUID tenantId, @RequestBody DataSourceModDTO dto) {
        return ResponseEntity.ok(dataSourceService.modifyDataSource(id, tenantId, dto.name(), dto.config()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDataSource(@PathVariable UUID id, @RequestHeader("X-Tenant-Id") UUID tenantId) {
        dataSourceService.deleteDataSource(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test-connection")
    public ResponseEntity<Void> testConnection(@RequestHeader("X-tenant-Id") UUID tenantId, @RequestBody TestConnectionRequestDTO dto) {
        dataSourceService.testConnection(dto.type(), dto.config());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/schema")
    public ResponseEntity<List<TableMetadata>> getSchema(@PathVariable UUID id, @RequestHeader("X-Tenant-Id") UUID tenantId) {
        List<TableMetadata> schema = dataSourceService.getSchema(id, tenantId);
        return ResponseEntity.ok(schema);
    }

    @PutMapping("/{id}/selected-tables")
    public ResponseEntity<DataSource> updateSelectedTables(@PathVariable UUID id, @RequestHeader("X-Tenant-Id") UUID tenantId, @RequestBody SelectedTablesDTO dto) {
        DataSource ds = dataSourceService.updateSelectedTables(id, tenantId, dto.selectedTables());
        return ResponseEntity.ok(ds);
    }
}