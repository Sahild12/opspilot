package com.opspilot.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opspilot.backend.domain.Incident;
import com.opspilot.backend.domain.ServiceEntity;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findAllByOrderByStartedAtDesc();
    Optional<Incident> findFirstByServiceAndStatus(ServiceEntity service, String status);
}