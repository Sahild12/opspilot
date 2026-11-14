package com.opspilot.backend.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LogRequest(
        @NotBlank @Size(max = 100) String service,
        @NotBlank @Pattern(regexp = "TRACE|DEBUG|INFO|WARN|ERROR|FATAL") String level,
        @NotBlank String message,
        @Size(max = 64) String traceId,
        Instant timestamp
) {}