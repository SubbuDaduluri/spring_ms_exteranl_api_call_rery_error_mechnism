package com.subbu.dsrmtech.externalapicall.service;

import com.subbu.dsrmtech.externalapicall.model.Posts;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ExternalApiService {

    ResponseEntity<List<Posts>> callExternalApi();

    ResponseEntity<List<Posts>> callExternalApiRestClientRetry();
    ResponseEntity<List<Posts>> callExternalApiWebclientRetry();
}
