package com.opspilot.backend.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opspilot.backend.domain.LogEvent;
import com.opspilot.backend.domain.ServiceEntity;
import com.opspilot.backend.dto.LogRequest;
import com.opspilot.backend.repository.LogEventRepository;
import com.opspilot.backend.repository.ServiceRepository;

@Service
public class LogIngestionService {

    private final ServiceRepository serviceRepository;
    private final LogEventRepository logEventRepository;
    private final ErrorCounter errorCounter;

    public LogIngestionService(ServiceRepository serviceRepository,
                               LogEventRepository logEventRepository,
                               ErrorCounter errorCounter) {
        this.serviceRepository = serviceRepository;
        this.logEventRepository = logEventRepository;
        this.errorCounter = errorCounter;
    }

    @Transactional
    public Long ingest(LogRequest req) {
        ServiceEntity svc = serviceRepository.findByName(req.service())
                .orElseGet(() -> serviceRepository.save(new ServiceEntity(req.service())));

        Instant when = req.timestamp() != null ? req.timestamp() : Instant.now();

        LogEvent event = new LogEvent(svc, req.level(), req.message(), req.traceId(), when);
        Long id = logEventRepository.save(event).getId();

        if ("ERROR".equals(req.level()) || "FATAL".equals(req.level())) {
            try {
                errorCounter.recordError(svc.getName());
           } catch (Exception e) {
          System.err.println("REDIS COUNT FAILED: " + e);
    }
        }
        return id;
    }
}