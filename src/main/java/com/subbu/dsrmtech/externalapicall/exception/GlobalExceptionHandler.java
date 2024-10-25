package com.subbu.dsrmtech.externalapicall.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<String> handleRestClientException(RestClientException ex) {
        return new ResponseEntity<>("API connection issue: " + ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<String> handleRestClientException(ExternalApiException ex) {
        return new ResponseEntity<>("External API error: " + ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler({HttpClientErrorException.class})
    public ResponseEntity<String> handleHttpClientError(HttpClientErrorException ex) {
        return new ResponseEntity<>("Error from external API: " + ex.getMessage(), ex.getStatusCode());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
