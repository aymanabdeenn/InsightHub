package com.ayman.configlib.error;

public class ApiException extends RuntimeException {
    private final String errorCode;

    public ApiException(String errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public ApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
