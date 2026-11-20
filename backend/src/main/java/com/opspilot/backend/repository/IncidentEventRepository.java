package com.opspilot.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opspilot.backend.domain.IncidentEvent;

public interface IncidentEventRepository extends JpaRepository<IncidentEvent, Long> {
    List<IncidentEvent> findByIncidentIdOrderByOccurredAtAsc(Long incidentId);
}