package com.ayman.datasourceservice.repository;

import com.ayman.datasourceservice.domain.ConnectionStats;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ConnectionStatsRepository extends MongoRepository<ConnectionStats, UUID> {
}
