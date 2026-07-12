package com.ayman.configlib.error;

public class DataSourceNotFoundException extends ApiException{

    public DataSourceNotFoundException(String errorCode)  {
        super(errorCode);
    }

    public DataSourceNotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }

}
