package com.subbu.dsrmtech.externalapicall.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${retry.max-attempts}")
    private String retryMaxAttempts;
    @Value("${retry.back-off-delay}")
    private String retryBackOffDelay;

    public String getRetryMaxAttempts() {
        return retryMaxAttempts;
    }

    public void setRetryMaxAttempts(String retryMaxAttempts) {
        this.retryMaxAttempts = retryMaxAttempts;
    }

    public String getRetryBackOffDelay() {
        return retryBackOffDelay;
    }

    public void setRetryBackOffDelay(String retryBackOffDelay) {
        this.retryBackOffDelay = retryBackOffDelay;
    }
}
