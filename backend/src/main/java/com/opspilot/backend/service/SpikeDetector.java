package com.opspilot.backend.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.opspilot.backend.domain.ServiceEntity;
import com.opspilot.backend.repository.ServiceRepository;

@Component
public class SpikeDetector {

    private static final Logger log = LoggerFactory.getLogger(SpikeDetector.class);

    private static final int BASELINE_MINUTES = 10;
    private static final long MIN_ERRORS = 10;
    private static final double MULTIPLIER = 3.0;
    private static final long CALM_LEVEL = 3;
    private static final long BASELINE_CAP = 10; // a past spike minute can't count as "normal"

    private final ServiceRepository serviceRepository;
    private final ErrorCounter counter;
    private final IncidentService incidentService;

    public SpikeDetector(ServiceRepository serviceRepository,
                         ErrorCounter counter,
                         IncidentService incidentService) {
        this.serviceRepository = serviceRepository;
        this.counter = counter;
        this.incidentService = incidentService;
    }

    @Scheduled(fixedDelay = 10_000)
    public void check() {
        long current = ErrorCounter.minuteBucket(Instant.now());

        for (ServiceEntity svc : serviceRepository.findAll()) {
            String name = svc.getName();
            long now = counter.count(name, current);
            long previous = counter.count(name, current - 1);

            double sum = 0;
            for (int i = 1; i <= BASELINE_MINUTES; i++) {
                sum += Math.min(counter.count(name, current - i), BASELINE_CAP);
            }
            double baseline = Math.max(sum / BASELINE_MINUTES, 1.0);

            log.info("CHECK service={} errorsThisMinute={} baseline={}",
                    name, now, String.format("%.1f", baseline));

            try {
                if (now >= MIN_ERRORS && now >= baseline * MULTIPLIER && counter.tryMarkSpike(name)) {
                    log.warn("SPIKE DETECTED service={} errorsThisMinute={} baseline={}",
                            name, now, String.format("%.1f", baseline));
                    incidentService.openOrUpdate(svc, now, baseline);
                } else if (now < CALM_LEVEL && previous < CALM_LEVEL) {
                    incidentService.resolveIfOpen(svc);
                }
            } catch (Exception e) {
                log.error("Incident handling failed for {}", name, e);
            }
        }
    }
}