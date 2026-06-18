package com.ayman.configlib.errorhandler.dto;

import java.util.UUID;

public record GenericResponseDTO<T>(UUID correlationId, Long code, String message, String status, T returnedData) {
}
