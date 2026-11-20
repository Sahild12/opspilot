package com.opspilot.backend.dto;

import java.time.Instant;
import java.util.List;

public record IncidentDto(
        Long id,
        String service,
        String title,
        String status,
        String severity,
        Instant startedAt,
        Instant resolvedAt,
        long peakErrorsPerMin,
        String summary,
        List<TimelineItem> timeline
) {
    public record TimelineItem(Instant time, String type, String message) {}
}