package com.ayman.configlib.error;

public class DuplicateDataSourceException extends ApiException{
    public DuplicateDataSourceException(String message) {
        super(message);
    }

    public DuplicateDataSourceException(String errorCode, String message) { super(errorCode, message); }
}
