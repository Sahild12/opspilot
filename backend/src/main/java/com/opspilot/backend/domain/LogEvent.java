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
@Table(name = "log_event")
public class LogEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @Column(nullable = false, length = 10)
    private String level;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "logged_at", nullable = false)
    private Instant loggedAt;

    @Column(name = "received_at", nullable = false, insertable = false, updatable = false)
    private Instant receivedAt;

    protected LogEvent() {}

    public LogEvent(ServiceEntity service, String level, String message,
                    String traceId, Instant loggedAt) {
        this.service = service;
        this.level = level;
        this.message = message;
        this.traceId = traceId;
        this.loggedAt = loggedAt;
    }

    public Long getId() { return id; }
    public ServiceEntity getService() { return service; }
    public String getLevel() { return level; }
    public String getMessage() { return message; }
    public String getTraceId() { return traceId; }
    public Instant getLoggedAt() { return loggedAt; }
}