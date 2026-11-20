package com.opspilot.backend.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "incident")
public class Incident {

    public static final String OPEN = "OPEN";
    public static final String RESOLVED = "RESOLVED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String severity;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "peak_errors_per_min", nullable = false)
    private long peakErrorsPerMin;

    @Column(columnDefinition = "TEXT")
    private String summary;

    protected Incident() {}

    public Incident(ServiceEntity service, String title, String severity,
                    Instant startedAt, long peakErrorsPerMin) {
        this.service = service;
        this.title = title;
        this.severity = severity;
        this.startedAt = startedAt;
        this.peakErrorsPerMin = peakErrorsPerMin;
        this.status = OPEN;
    }

    public void resolve(Instant when) {
        this.status = RESOLVED;
        this.resolvedAt = when;
    }

    public void updatePeak(long errors) {
        if (errors > peakErrorsPerMin) peakErrorsPerMin = errors;
    }

    public Long getId() { return id; }
    public ServiceEntity getService() { return service; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getSeverity() { return severity; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public long getPeakErrorsPerMin() { return peakErrorsPerMin; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}