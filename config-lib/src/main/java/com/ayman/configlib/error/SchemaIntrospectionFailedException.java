package com.ayman.configlib.error;

public class SchemaIntrospectionFailedException extends ApiException {
    public SchemaIntrospectionFailedException(String message) {
        super(message);
    }
    public SchemaIntrospectionFailedException(String errorCode, String message) { super(errorCode, message); }
}
