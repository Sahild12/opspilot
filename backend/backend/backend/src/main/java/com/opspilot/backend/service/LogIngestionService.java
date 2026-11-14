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

    public LogIngestionService(ServiceRepository serviceRepository,
                               LogEventRepository logEventRepository) {
        this.serviceRepository = serviceRepository;
        this.logEventRepository = logEventRepository;
    }

    @Transactional
    public Long ingest(LogRequest req) {
        ServiceEntity svc = serviceRepository.findByName(req.service())
                .orElseGet(() -> serviceRepository.save(new ServiceEntity(req.service())));

        Instant when = req.timestamp() != null ? req.timestamp() : Instant.now();

        LogEvent event = new LogEvent(svc, req.level(), req.message(), req.traceId(), when);
        return logEventRepository.save(event).getId();
    }
}