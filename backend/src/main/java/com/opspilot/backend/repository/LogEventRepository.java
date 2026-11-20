package com.opspilot.backend.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opspilot.backend.domain.LogEvent;

public interface LogEventRepository extends JpaRepository<LogEvent, Long> {

    /** Returns rows of [message, count], most frequent first. */
    @Query("""
           select l.message, count(l) from LogEvent l
           where l.service.id = :serviceId
             and l.level in ('ERROR','FATAL')
             and l.loggedAt >= :since
           group by l.message
           order by count(l) desc
           """)
    List<Object[]> topErrors(@Param("serviceId") Long serviceId, @Param("since") Instant since);
}