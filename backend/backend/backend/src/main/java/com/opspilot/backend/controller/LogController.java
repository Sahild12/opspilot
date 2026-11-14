package com.opspilot.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.opspilot.backend.dto.LogRequest;
import com.opspilot.backend.service.LogIngestionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogIngestionService ingestionService;

    public LogController(LogIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> receive(@Valid @RequestBody LogRequest request) {
        return Map.of("id", ingestionService.ingest(request));
    }
}