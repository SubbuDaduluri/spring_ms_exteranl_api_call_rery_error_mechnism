package com.subbu.dsrmtech.externalapicall.exception;


public class ExternalServiceUnavailableException extends ExternalApiException {
    public ExternalServiceUnavailableException(String message) {
        super(message);
    }

    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
