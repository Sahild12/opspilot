package com.opspilot.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.opspilot.backend.dto.IncidentDto;

@Service
public class IncidentAnalysisService {

    private final IncidentService incidents;
    private final RestClient client;

    public IncidentAnalysisService(IncidentService incidents,
                                   @Value("${opspilot.ai.url}") String aiUrl) {
        this.incidents = incidents;
        this.client = RestClient.builder().baseUrl(aiUrl).build();
    }

    public IncidentDto analyze(Long id) {
        IncidentDto incident = incidents.get(id);
        String result;
        try {
            result = client.post().uri("/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(incident)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI service unavailable");
        }
        incidents.saveSummary(id, result);
        return incidents.get(id);
    }
}