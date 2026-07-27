package com.ayman.configlib.response;

public record GenericResponseDTO<T>(String correlationId, String code, String message, String explanation, Long status, T returnedData) {
}
