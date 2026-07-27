package com.ayman.datasourceservice.repository;

import com.ayman.datasourceservice.domain.DataSource;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataSourceRepository extends MongoRepository<DataSource, UUID> {
    List<DataSource> findByTenantId(UUID tenantId);
    Optional<DataSource> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndName(UUID tenantId, String name);
}
