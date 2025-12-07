package com.iwaproject.logging.service;

import com.iwaproject.logging.model.LogEntry;
import com.iwaproject.logging.model.LogMessage;
import com.iwaproject.logging.repository.LogEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing log entries
 */
@Service
@Slf4j
public class LogService {

    private final LogEntryRepository logEntryRepository;

    public LogService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    /**
     * Save a log message to the database
     */
    @Transactional
    public LogEntry saveLog(LogMessage logMessage) {
        LogEntry logEntry = LogEntry.builder()
            .serviceName(logMessage.getServiceName())
            .logLevel(logMessage.getLogLevel())
            .message(logMessage.getMessage())
            .timestamp(logMessage.getTimestamp() != null ? logMessage.getTimestamp() : LocalDateTime.now())
            .threadName(logMessage.getThreadName())
            .loggerName(logMessage.getLoggerName())
            .exceptionMessage(logMessage.getExceptionMessage())
            .stackTrace(logMessage.getStackTrace())
            .correlationId(logMessage.getCorrelationId())
            .userId(logMessage.getUserId())
            .requestUri(logMessage.getRequestUri())
            .requestMethod(logMessage.getRequestMethod())
            .responseStatus(logMessage.getResponseStatus())
            .build();

        return logEntryRepository.save(logEntry);
    }

    /**
     * Get logs by service name
     */
    public Page<LogEntry> getLogsByService(String serviceName, Pageable pageable) {
        return logEntryRepository.findByServiceName(serviceName, pageable);
    }

    /**
     * Get logs by log level
     */
    public Page<LogEntry> getLogsByLevel(String logLevel, Pageable pageable) {
        return logEntryRepository.findByLogLevel(logLevel, pageable);
    }

    /**
     * Get logs within a time range
     */
    public Page<LogEntry> getLogsByTimeRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return logEntryRepository.findByTimestampBetween(start, end, pageable);
    }

    /**
     * Get logs by correlation ID
     */
    public List<LogEntry> getLogsByCorrelationId(String correlationId) {
        return logEntryRepository.findByCorrelationIdOrderByTimestampAsc(correlationId);
    }

    /**
     * Get error logs
     */
    public Page<LogEntry> getErrorLogs(Pageable pageable) {
        return logEntryRepository.findErrorLogs(pageable);
    }

    /**
     * Get log statistics for a service
     */
    public long countLogsByServiceAndLevel(String serviceName, String logLevel) {
        return logEntryRepository.countByServiceNameAndLogLevel(serviceName, logLevel);
    }

    /**
     * Clean up old logs (scheduled task)
     * Runs every day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldLogs() {
        // Keep logs for 30 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        
        log.info("Starting cleanup of logs older than {}", cutoffDate);
        
        try {
            logEntryRepository.deleteByTimestampBefore(cutoffDate);
            log.info("Successfully cleaned up old logs");
        } catch (Exception e) {
            log.error("Error cleaning up old logs: {}", e.getMessage(), e);
        }
    }
}
