package com.subbu.dsrmtech.externalapicall.handler;

import com.subbu.dsrmtech.externalapicall.exception.ExternalApiException;
import com.subbu.dsrmtech.externalapicall.exception.ExternalServiceUnavailableException;
import com.subbu.dsrmtech.externalapicall.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
@Slf4j
public class CustomErrorHandler {

    public Mono<Throwable> handleClientError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
            .flatMap(errorBody -> {
                if (clientResponse.statusCode().is4xxClientError()) {
                    log.error("Server Error (4xx): " + errorBody);
                    return Mono.error(new ResourceNotFoundException("Resource not found: " + errorBody));
                } else if (clientResponse.statusCode().is5xxServerError()) {
                    log.error("Server Error (5xx): " + errorBody);
                    return Mono.error(new ExternalServiceUnavailableException("Service is currently unavailable: " + errorBody));
                }
                return Mono.error(new ExternalApiException("Unexpected error: " + errorBody));
            });
    }

    public Mono<Throwable> handleOtherExceptions(Throwable throwable) {
        if (throwable.getCause() instanceof IOException) {
            // Handle IOException
            log.error("Network I/O Error: " + throwable.getCause().getMessage());
            return Mono.error(new ExternalServiceUnavailableException("External service is unavailable due to Network Error: " + throwable.getCause().getMessage()));
        }
        throw new ExternalApiException("An error occurred in external API call: " + throwable.getMessage());
    }
}

