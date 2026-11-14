package com.opspilot.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opspilot.backend.domain.LogEvent;

public interface LogEventRepository extends JpaRepository<LogEvent, Long> {
}