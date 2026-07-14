package com.ayman.configlib.error;

public class ConnectionTestFailedException extends ApiException{

    public ConnectionTestFailedException(String message) {
        super(message);
    }

    public ConnectionTestFailedException(String errorCode, String message) {super(errorCode, message);}

}
