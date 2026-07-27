package com.ayman.datasourceservice.domain.dto;

public record ConnectionConfigResponseDTO(String host, int port, String database, String username) {
}
