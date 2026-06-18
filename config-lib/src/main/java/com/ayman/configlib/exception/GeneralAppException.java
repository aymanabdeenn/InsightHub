package com.ayman.configlib.exception;

import com.ayman.configlib.errorhandler.dto.GenericResponseDTO;

public class GeneralAppException extends RuntimeException {
    private final GenericResponseDTO<?> response;

    public GeneralAppException(String message, GenericResponseDTO<?> response) {
        super(message);
        this.response = response;
    }

    public GenericResponseDTO<?> getResponse() {return this.response;}
}
