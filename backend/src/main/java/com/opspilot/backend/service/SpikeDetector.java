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

    private final ServiceRepository serviceRepository;
    private final ErrorCounter counter;

    public SpikeDetector(ServiceRepository serviceRepository, ErrorCounter counter) {
        this.serviceRepository = serviceRepository;
        this.counter = counter;
    }

    @Scheduled(fixedDelay = 10_000)
    public void check() {
        long current = ErrorCounter.minuteBucket(Instant.now());

        for (ServiceEntity svc : serviceRepository.findAll()) {
            String name = svc.getName();
            long now = counter.count(name, current);

            double sum = 0;
            for (int i = 1; i <= BASELINE_MINUTES; i++) {
                sum += counter.count(name, current - i);
            }
            double baseline = Math.max(sum / BASELINE_MINUTES, 1.0);
            log.info("CHECK service={} errorsThisMinute={} baseline={}", name, now, String.format("%.1f", baseline));

            if (now >= MIN_ERRORS && now >= baseline * MULTIPLIER && counter.tryMarkSpike(name)) {
                log.warn("SPIKE DETECTED service={} errorsThisMinute={} baseline={}",
                        name, now, String.format("%.1f", baseline));
                // Step 5: create an Incident here
            }
        }
    }
}