package com.subbu.dsrmtech.externalapicall.service.impl;

import com.subbu.dsrmtech.externalapicall.config.AppConfig;
import com.subbu.dsrmtech.externalapicall.config.RetryConfig;
import com.subbu.dsrmtech.externalapicall.exception.ExternalApiException;
import com.subbu.dsrmtech.externalapicall.exception.ExternalServiceUnavailableException;
import com.subbu.dsrmtech.externalapicall.exception.ResourceNotFoundException;
import com.subbu.dsrmtech.externalapicall.handler.CustomErrorHandler;
import com.subbu.dsrmtech.externalapicall.model.Posts;
import com.subbu.dsrmtech.externalapicall.service.ExternalApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ExternalApiServiceImpl implements ExternalApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RetryTemplate retryTemplate;

    @Autowired
    private WebClient webClient;

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private CustomErrorHandler customErrorHandler;

    @Autowired
    private RetryConfig retryConfig;

    @Autowired
    private RestClient restClient;


    @Override
    public ResponseEntity<List<Posts>> callExternalApi() {
        return retryTemplate.execute(retryContext -> {
            // Your API call logic here
            return fetchData(retryContext);
        }, retryContext -> {
            // This is the recovery callback
            System.out.println("Recovery method called due to: " + retryContext.getLastThrowable().getMessage());
            // Perform recovery logic, such as logging or notifying
            // Return a value or throw an exception if needed
            throw new ExternalApiException("API call Error " + retryContext.getLastThrowable().getMessage());
        });
    }

    @Override
    public ResponseEntity<List<Posts>> callExternalApiWebclientRetry() {
        return fetchDataUsingWebClient();
    }

    @Override
    public ResponseEntity<List<Posts>> callExternalApiRestClientRetry() {
        return retryTemplate.execute(retryContext -> {
            // Your API call logic here
            return fetchDataUsingRestClient();
        }, retryContext -> {
            // This is the recovery callback
            System.out.println("Recovery method called due to: " + retryContext.getLastThrowable().getMessage());
            // Perform recovery logic, such as logging or notifying
            // Return a value or throw an exception if needed
            throw new ExternalApiException("API call Error " + retryContext.getLastThrowable().getMessage());
        });
    }

    private ResponseEntity<List<Posts>> fetchData(RetryContext retryContext) {

        try {
            ResponseEntity<List<Posts>> responseEntity = restTemplate.exchange(
                // "https://jsonplaceholder.typicode.com/posts12",
                "https://external-api.com/endpoint", //
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Posts>>() {
                });
            return responseEntity;
        } catch (HttpStatusCodeException e) {
            // Handle specific HTTP error responses (5xx server error && 4xx client errors)
            if (e.getStatusCode().is5xxServerError()) {
                log.error("Server Error (5xx): " + e.getCause().getMessage());
                throw new ExternalServiceUnavailableException("External service is unavailable");
            } else if (e.getStatusCode().is4xxClientError()) {
                log.error("Client Error(4xx) : " + e.getCause().getMessage());
                throw new ResourceNotFoundException("Resource not found: " + e.getStatusCode());
            }
            throw new ExternalApiException("An error occurred in external API call: " + e.getMessage());
        } catch (RestClientException e) {
            // Handle RestClient-related exceptions, including potential IOExceptions
            if (e.getCause() instanceof IOException) {
                log.error("Network I/O Error: " + e.getCause().getMessage());
                throw new ExternalServiceUnavailableException("External service is unavailable due to Network Error: " + e.getCause().getMessage());
            }
            // Handle other RestTemplate-related exceptions
            log.error("RestClient Error: " + e.getMessage());
            throw new ExternalApiException("An error occurred in external API call: " + e.getMessage());
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            log.error("Unexpected Error: " + e.getMessage());
            throw new ExternalApiException("An error occurred in external API call: " + e.getMessage());
        }

    }

    private ResponseEntity<List<Posts>> fetchDataUsingRestClient() {
        try {
            List<Posts> postsList = restClient.get()
                //.uri("https://jsonplaceholder.typicode.com/posts12")
                .uri("https://external-api.com/endpoint")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Posts>>() {
                });
            return new ResponseEntity<>(postsList, HttpStatus.OK);
        } catch (HttpStatusCodeException e) {
            // Handle specific HTTP error responses (5xx server error && 4xx client errors)
            if (e.getStatusCode().is5xxServerError()) {
                log.error("Server Error (5xx): " + e.getMessage());
                throw new ExternalServiceUnavailableException("External service is unavailable"+ e.getMessage());
            } else if (e.getStatusCode().is4xxClientError()) {
                log.error("Client Error(4xx) : " + e.getMessage());
                throw new ResourceNotFoundException("Resource not found: " + e.getMessage());
            }
            throw new ExternalApiException("An error occurred in external API call: " + e.getMessage());
        } catch (RestClientException e) {
            // Handle RestClient-related exceptions, including potential IOExceptions
            if (e.getCause() instanceof IOException) {
                log.error("Network I/O Error: " + e.getMessage());
                throw new ExternalServiceUnavailableException("External service is unavailable due to Network Error: " + e.getCause().getMessage());
            }
            // Handle other RestTemplate-related exceptions
            log.error("RestClient Error: " + e.getMessage());
            throw new ExternalApiException("An error occurred in external API call: " + e.getMessage());
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            log.error("Unexpected Error: " + e.getMessage());
            throw new ExternalApiException("An error occurred in external API call: " + e.getMessage());
        }
    }

    private ResponseEntity<List<Posts>> fetchDataUsingWebClient() {

        List<Posts> postsList = webClient.get()
            .uri("https://jsonplaceholder.typicode.com/posts12")
            // .uri("https://external-api.com/endpoint")
            .retrieve()
            .onStatus(HttpStatusCode::isError, customErrorHandler::handleClientError) // Handle HTTP errors
            .bodyToFlux(Posts.class)
            .collectList() // Collect into a List<Posts>
            .retryWhen(retryConfig.retryableExceptions())  // Retry logic using custom exceptions
            .doOnError(throwable -> {
                // Handle other exceptions
                System.out.println("Error occurred: " + throwable.getMessage());
                throw new ExternalApiException("An error occurred in external API call: " + throwable.getMessage());
            })
            .block();
        return new ResponseEntity<>(postsList, HttpStatus.OK);
    }

}
