package com.opspilot.backend.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.opspilot.backend.domain.Incident;
import com.opspilot.backend.domain.IncidentEvent;
import com.opspilot.backend.domain.ServiceEntity;
import com.opspilot.backend.dto.IncidentDto;
import com.opspilot.backend.repository.IncidentEventRepository;
import com.opspilot.backend.repository.IncidentRepository;
import com.opspilot.backend.repository.LogEventRepository;

@Service
public class IncidentService {

    private final IncidentRepository incidents;
    private final IncidentEventRepository events;
    private final LogEventRepository logs;

    public IncidentService(IncidentRepository incidents,
                           IncidentEventRepository events,
                           LogEventRepository logs) {
        this.incidents = incidents;
        this.events = events;
        this.logs = logs;
    }

    /** Called by the spike detector. Opens a new incident, or adds to the one already open. */
    @Transactional
    public void openOrUpdate(ServiceEntity svc, long errorsPerMin, double baseline) {
        Instant now = Instant.now();
        String spikeText = String.format("Error spike: %d errors in the last minute (normal is about %.1f)",
                errorsPerMin, baseline);

        Optional<Incident> existing = incidents.findFirstByServiceAndStatus(svc, Incident.OPEN);
        if (existing.isPresent()) {
            Incident inc = existing.get();
            inc.updatePeak(errorsPerMin);
            events.save(new IncidentEvent(inc, now, "SPIKE_DETECTED", spikeText));
            return;
        }

        String severity = errorsPerMin >= 30 ? "HIGH" : "MEDIUM";
        Incident inc = incidents.save(new Incident(
                svc, "Error spike on " + svc.getName(), severity, now, errorsPerMin));

        events.save(new IncidentEvent(inc, now, "SPIKE_DETECTED", spikeText));

        // Evidence: the most common error messages in the last 5 minutes
        List<Object[]> top = logs.topErrors(svc.getId(), now.minus(Duration.ofMinutes(5)));
        top.stream().limit(5).forEach(row ->
                events.save(new IncidentEvent(inc, now, "ERROR_SAMPLE",
                        row[1] + " times: " + row[0])));
    }

    /** Called by the spike detector when errors look normal again. */
    @Transactional
    public void resolveIfOpen(ServiceEntity svc) {
        incidents.findFirstByServiceAndStatus(svc, Incident.OPEN).ifPresent(inc -> {
            Instant now = Instant.now();
            inc.resolve(now);
            events.save(new IncidentEvent(inc, now, "AUTO_RESOLVED",
                    "Error rate returned to normal. Incident closed automatically."));
        });
    }

    @Transactional
    public IncidentDto resolveManually(Long id) {
        Incident inc = incidents.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));
        if (Incident.OPEN.equals(inc.getStatus())) {
            Instant now = Instant.now();
            inc.resolve(now);
            events.save(new IncidentEvent(inc, now, "RESOLVED", "Marked as resolved by a person."));
        }
        return toDto(inc, true);
    }

    @Transactional(readOnly = true)
    public List<IncidentDto> list() {
        return incidents.findAllByOrderByStartedAtDesc().stream()
                .map(i -> toDto(i, false)).toList();
    }

    @Transactional(readOnly = true)
    public IncidentDto get(Long id) {
        Incident inc = incidents.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));
        return toDto(inc, true);
    }

    @Transactional
public void saveSummary(Long id, String summary) {
    incidents.findById(id).ifPresent(i -> i.setSummary(summary));
}

    private IncidentDto toDto(Incident i, boolean withTimeline) {
        List<IncidentDto.TimelineItem> timeline = withTimeline
                ? events.findByIncidentIdOrderByOccurredAtAsc(i.getId()).stream()
                    .map(e -> new IncidentDto.TimelineItem(e.getOccurredAt(), e.getType(), e.getMessage()))
                    .toList()
                : null;
        return new IncidentDto(i.getId(), i.getService().getName(), i.getTitle(), i.getStatus(),
                i.getSeverity(), i.getStartedAt(), i.getResolvedAt(), i.getPeakErrorsPerMin(),
                i.getSummary(), timeline);
    }
}