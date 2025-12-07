package com.iwaproject.logging.repository;

import com.iwaproject.logging.model.LogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for LogEntry entities
 */
@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    /**
     * Find logs by service name
     */
    Page<LogEntry> findByServiceName(String serviceName, Pageable pageable);

    /**
     * Find logs by log level
     */
    Page<LogEntry> findByLogLevel(String logLevel, Pageable pageable);

    /**
     * Find logs by service name and log level
     */
    Page<LogEntry> findByServiceNameAndLogLevel(String serviceName, String logLevel, Pageable pageable);

    /**
     * Find logs within a time range
     */
    Page<LogEntry> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Find logs by service name within a time range
     */
    Page<LogEntry> findByServiceNameAndTimestampBetween(
        String serviceName, 
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );

    /**
     * Find logs by correlation ID
     */
    List<LogEntry> findByCorrelationIdOrderByTimestampAsc(String correlationId);

    /**
     * Find error logs (ERROR and FATAL)
     */
    @Query("SELECT l FROM LogEntry l WHERE l.logLevel IN ('ERROR', 'FATAL') ORDER BY l.timestamp DESC")
    Page<LogEntry> findErrorLogs(Pageable pageable);

    /**
     * Count logs by service name and log level
     */
    @Query("SELECT COUNT(l) FROM LogEntry l WHERE l.serviceName = :serviceName AND l.logLevel = :logLevel")
    long countByServiceNameAndLogLevel(@Param("serviceName") String serviceName, @Param("logLevel") String logLevel);

    /**
     * Delete old logs (older than specified date)
     */
    void deleteByTimestampBefore(LocalDateTime cutoffDate);
}
