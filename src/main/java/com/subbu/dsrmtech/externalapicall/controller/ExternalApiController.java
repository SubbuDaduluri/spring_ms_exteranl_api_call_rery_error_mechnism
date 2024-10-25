package com.subbu.dsrmtech.externalapicall.controller;

import com.subbu.dsrmtech.externalapicall.model.Posts;
import com.subbu.dsrmtech.externalapicall.service.ExternalApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/external")
public class ExternalApiController {

    @Autowired
    private ExternalApiService externalApiService;


    @GetMapping("/call")
    public ResponseEntity<List<Posts>> callExternalApi() {
        return externalApiService.callExternalApi();
    }

    @GetMapping("/retry-call")
    public ResponseEntity<List<Posts>> callExternalApiWebclientRetry() {
        return externalApiService.callExternalApiWebclientRetry();
    }

    @GetMapping("/restclient-call")
    public ResponseEntity<List<Posts>> callExternalApiRestClientRetry() {
        return externalApiService.callExternalApiRestClientRetry();
    }
}
