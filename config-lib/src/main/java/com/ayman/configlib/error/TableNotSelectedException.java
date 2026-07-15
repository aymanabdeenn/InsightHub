package com.ayman.configlib.error;

public class TableNotSelectedException extends ApiException {
    public TableNotSelectedException(String message) {
        super(message);
    }
    public TableNotSelectedException(String errorCode, String message) { super(errorCode, message); }
}
