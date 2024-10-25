package com.subbu.dsrmtech.externalapicall.config;

import com.subbu.dsrmtech.externalapicall.exception.ExternalServiceUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Component
public class RetryTemplateConfig {

    @Autowired
    private AppConfig appConfig;

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Define the backoff policy
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(Long.parseLong(appConfig.getRetryBackOffDelay()));//  Wait specified seconds before retrying
        retryTemplate.setBackOffPolicy(backOffPolicy);


        // Configure retry policy
        int maxAttempts = Integer.parseInt(appConfig.getRetryMaxAttempts());  // Retry specified times
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(IOException.class, true);
        retryableExceptions.put(UnknownHostException.class, true); // Retry on this exception
        retryableExceptions.put(ExternalServiceUnavailableException.class, true); // Retry on this exception


        // Define the retry policy
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, retryableExceptions);
        retryPolicy.setMaxAttempts(maxAttempts);
        retryTemplate.setRetryPolicy(retryPolicy);


        return retryTemplate;
    }
}
