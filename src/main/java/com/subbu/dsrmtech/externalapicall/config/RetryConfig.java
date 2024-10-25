package com.subbu.dsrmtech.externalapicall.config;

import com.subbu.dsrmtech.externalapicall.exception.ExternalServiceUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class RetryConfig {

    private final Duration backoffDuration = Duration.ofSeconds(2);

    @Autowired
    private AppConfig appConfig;

    public Retry retryableExceptions() {
        return Retry.fixedDelay(Long.parseLong(appConfig.getRetryMaxAttempts()), Duration.ofMillis(1000))
            .maxBackoff(Duration.ofMillis(Long.parseLong(appConfig.getRetryBackOffDelay())))
            .doBeforeRetry(retrySignal -> System.out.println("Retrying attempt #" + (retrySignal.totalRetries() + 1) +
                " due to error: " + retrySignal.failure().getMessage())) // Log each retry attempt
            .filter(this::isRetryableException);
    }

    private boolean isRetryableException(Throwable throwable) {
        // Check if the exception is transient and should be retried
        return (throwable instanceof WebClientResponseException &&
            ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
            || throwable.getCause() instanceof IOException || throwable instanceof ExternalServiceUnavailableException;
    }

}
