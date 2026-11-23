package com.opspilot.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opspilot.backend.dto.IncidentDto;
import com.opspilot.backend.service.IncidentAnalysisService;
import com.opspilot.backend.service.IncidentService;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService service;
    private final IncidentAnalysisService analysisService;

    public IncidentController(IncidentService service, IncidentAnalysisService analysisService) {
        this.service = service;
        this.analysisService = analysisService;
    }

    @GetMapping
    public List<IncidentDto> list() { return service.list(); }

    @GetMapping("/{id}")
    public IncidentDto get(@PathVariable Long id) { return service.get(id); }

    @PostMapping("/{id}/resolve")
    public IncidentDto resolve(@PathVariable Long id) { return service.resolveManually(id); }

    @PostMapping("/{id}/analyze")
    public IncidentDto analyze(@PathVariable Long id) { return analysisService.analyze(id); }
}