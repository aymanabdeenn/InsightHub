package com.ayman.datasourceservice.api;

import com.ayman.configlib.response.ResponseCodeProperties;
import com.ayman.configlib.observability.correlation.CorrelationIdFilter;
import com.ayman.configlib.response.GenericResponseDTO;
import com.ayman.datasourceservice.domain.DataSource;
import com.ayman.datasourceservice.domain.TableMetadata;
import com.ayman.datasourceservice.domain.dto.*;
import com.ayman.datasourceservice.service.DataSourceService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/data-sources")
public class DataSourceController {
    private final DataSourceService dataSourceService;
    private final ResponseCodeProperties responseCodeProperties;
    private final MessageSource messageSource;

    @Autowired
    public DataSourceController(DataSourceService dataSourceService, ResponseCodeProperties responseCodeProperties, MessageSource messageSource) {
        this.dataSourceService = dataSourceService;
        this.responseCodeProperties = responseCodeProperties;
        this.messageSource = messageSource;
    }

    public DataSourceResponseDTO formDataSourceAsResponse(DataSource ds, UUID tenantId) {
        ConnectionConfigResponseDTO configDTO = new ConnectionConfigResponseDTO(ds.getConnectionConfig().getHost(), ds.getConnectionConfig().getPort(), ds.getConnectionConfig().getDatabase(), ds.getConnectionConfig().getUsername());
        DataSourceResponseDTO dto = new DataSourceResponseDTO(ds.getId(), tenantId, ds.getName(), ds.getType(), configDTO, ds.getSelectedTables(), ds.getCreatedAt(), ds.getUpdatedAt());
        return dto;
    }

    public String getCorrelationId() { return MDC.get(CorrelationIdFilter.MDC_KEY); }

    public ResponseCodeProperties.ResponseDetail  getResponseDetail(String code) {
        ResponseCodeProperties.ResponseDetail detail = responseCodeProperties.getCodes().get(code);
        if (detail == null) { // If there was a typo in the "code" variable or if the provided value wasn't written in the file.
            code = "INTERNAL_ERROR";
            detail = responseCodeProperties.getCodes().get(code);
        }

        return detail;
    }

    @GetMapping("/{id}")
    public GenericResponseDTO<DataSourceResponseDTO> getDataSource(@PathVariable UUID id, @RequestHeader("X-Tenant-Id") UUID tenantId, Locale locale) {
        DataSource ds = dataSourceService.retrieveDataSource(id, tenantId);
        DataSourceResponseDTO dsResponse = formDataSourceAsResponse(ds, tenantId);

        String correlationId = getCorrelationId();

        String code = "DATA_SOURCE_FOUND";
        ResponseCodeProperties.ResponseDetail detail = getResponseDetail(code);

        String message = messageSource.getMessage(code, null, locale);

        return new GenericResponseDTO<>(correlationId, code, message, "Date source was retrieved successfully.", (long)detail.getStatus(), dsResponse);
    }

    @GetMapping
    public GenericResponseDTO<List<DataSourceResponseDTO>> getAllDataSources(@RequestHeader("X-Tenant-Id") UUID tenantId, Locale locale) {
        List<DataSource> dataSources = dataSourceService.retrieveAllDataSourcesForTenant(tenantId);

        List<DataSourceResponseDTO> dsResponses = new ArrayList<>();
        for(DataSource ds : dataSources) dsResponses.add(formDataSourceAsResponse(ds, tenantId));

        String correlationId = getCorrelationId();

        String code = "DATA_SOURCE_FOUND";
        ResponseCodeProperties.ResponseDetail detail = getResponseDetail(code);

        String message = messageSource.getMessage(code, null, locale);

        return new GenericResponseDTO<>(correlationId, code, message, "Date sources for tenant " + tenantId.toString() + " were retrieved successfully.", (long)detail.getStatus(), dsResponses);
    }

    @PostMapping
    public GenericResponseDTO<DataSourceResponseDTO> registerDataSource(@RequestHeader("X-Tenant-Id") UUID tenantId, @RequestBody DataSourceRegistrationDTO dto, Locale locale) {
        DataSource ds = dataSourceService.registerDataSource(tenantId, dto.name(), dto.type(), dto.config());
        DataSourceResponseDTO dsResponse = formDataSourceAsResponse(ds, tenantId);

        String correlationId = getCorrelationId();

        String code = "DATA_SOURCE_CREATED";
        ResponseCodeProperties.ResponseDetail detail = getResponseDetail(code);

        String message = messageSource.getMessage(code, null, locale);

        return new GenericResponseDTO<>(correlationId, code, message, "Date source was created successfully.", (long)detail.getStatus(), dsResponse);
    }

    @PutMapping("/{id}")
    public GenericResponseDTO<DataSourceResponseDTO> updateDataSource(@PathVariable UUID id, @RequestHeader("X-Tenant-Id") UUID tenantId, @RequestBody DataSourceModDTO dto, Locale locale) {
        DataSource ds = dataSourceService.modifyDataSource(id, tenantId, dto.name(), dto.config());
        DataSourceResponseDTO dsResponse = formDataSourceAsResponse(ds, tenantId);

        String correlationId = getCorrelationId();

        String code = "DATA_SOURCE_MODIFIED";
        ResponseCodeProperties.ResponseDetail detail = getResponseDetail(code);

        String message = messageSource.getMessage(code, null, locale);

        return new GenericResponseDTO<>(correlationId, code, message, "Date source was modified successfully.", (long)detail.getStatus(), dsResponse);
    }

    @DeleteMapping("/{id}")
    public GenericResponseDTO<?> deleteDataSource(@PathVariable UUID id, @RequestHeader("X-Tenant-Id") UUID tenantId, Locale locale) {
        dataSourceService.deleteDataSource(id, tenantId);

        String correlationId = getCorrelationId();

        String code = "DATA_SOURCE_DELETED";
        ResponseCodeProperties.ResponseDetail detail = getResponseDetail(code);

        String message = messageSource.getMessage(code, null, locale);

        return new GenericResponseDTO<>(correlationId, code, message, "Date source was deleted successfully.", (long)detail.getStatus(), null);
    }

    @PostMapping("/test-connection")
    public GenericResponseDTO<?> testConnection(@RequestHeader("X-Tenant-Id") UUID tenantId, @RequestBody TestConnectionRequestDTO dto, Locale locale) {
        dataSourceService.testConnection(dto.type(), dto.config());

        String correlationId = getCorrelationId();

        String code = "CONNECTION_TEST_SUCCEEDED";
        ResponseCodeProperties.ResponseDetail detail = getResponseDetail(code);

        String message = messageSource.getMessage(code, null, locale);

        return new GenericResponseDTO<>(correlationId, code, message, "The connection to this data source is possible and the testing was successful.", (long)detail.getStatus(), null);
    }

    @GetMapping("/{id}/schema")
    public GenericResponseDTO<List<TableMetadata>> getSchema(@PathVariable UUID id, @RequestHeader("X-Tenant-Id") UUID tenantId, Locale locale) {
        List<TableMetadata> schema = dataSourceService.getSchema(id, tenantId);

        String correlationId = getCorrelationId();

        String code = "DATA_SOURCE_SCHEMA_OBTAINED";
        ResponseCodeProperties.ResponseDetail detail = getResponseDetail(code);

        String message = messageSource.getMessage(code, null, locale);

        return new GenericResponseDTO<>(correlationId, code, message, "The schema for the selected data source has been obtained successfully.", (long)detail.getStatus(), schema);
    }

    @PutMapping("/{id}/selected-tables")
    public GenericResponseDTO<DataSourceResponseDTO> updateSelectedTables(@PathVariable UUID id, @RequestHeader("X-Tenant-Id") UUID tenantId, @RequestBody SelectedTablesDTO dto, Locale locale) {
        DataSource ds = dataSourceService.updateSelectedTables(id, tenantId, dto.selectedTables());
        DataSourceResponseDTO dsResponse = formDataSourceAsResponse(ds, tenantId);

        String correlationId = getCorrelationId();

        String code = "DATA_SOURCE_SELECTED_TABLES_MODIFIED";
        ResponseCodeProperties.ResponseDetail detail = getResponseDetail(code);

        String message = messageSource.getMessage(code, null, locale);

        return new GenericResponseDTO<>(correlationId, code, message, "The selected tables of the given data source were modified successfully.", (long)detail.getStatus(), dsResponse);
    }

    @GetMapping("/{id}/tables/{tableName}/data")
    public GenericResponseDTO<Page<Map<String, Object>>> readTable(
            @PathVariable UUID id,
            @PathVariable String tableName,
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            Locale locale) {
        Page<Map<String, Object>> pageContent = dataSourceService.readTable(id, tenantId, tableName, page, size);

        String correlationId = getCorrelationId();

        String code = "DATA_SOURCE_READ";
        ResponseCodeProperties.ResponseDetail detail = getResponseDetail(code);

        String message = messageSource.getMessage(code, null, locale);

        return new GenericResponseDTO<>(correlationId, code, message, "The selected table was read successfully.", (long)detail.getStatus(), pageContent);
    }
}