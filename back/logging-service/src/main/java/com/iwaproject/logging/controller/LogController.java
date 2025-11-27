package com.iwaproject.logging.controller;

import com.iwaproject.logging.model.LogEntry;
import com.iwaproject.logging.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for querying logs
 */
@RestController
@RequestMapping("/api/logs")
@Slf4j
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * Get logs by service name
     */
    @GetMapping("/service/{serviceName}")
    public ResponseEntity<Page<LogEntry>> getLogsByService(
        @PathVariable String serviceName,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        @RequestParam(defaultValue = "timestamp") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<LogEntry> logs = logService.getLogsByService(serviceName, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get logs by level
     */
    @GetMapping("/level/{logLevel}")
    public ResponseEntity<Page<LogEntry>> getLogsByLevel(
        @PathVariable String logLevel,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<LogEntry> logs = logService.getLogsByLevel(logLevel.toUpperCase(), pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get error logs
     */
    @GetMapping("/errors")
    public ResponseEntity<Page<LogEntry>> getErrorLogs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LogEntry> logs = logService.getErrorLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get logs within a time range
     */
    @GetMapping("/time-range")
    public ResponseEntity<Page<LogEntry>> getLogsByTimeRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<LogEntry> logs = logService.getLogsByTimeRange(start, end, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get logs by correlation ID
     */
    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<List<LogEntry>> getLogsByCorrelationId(@PathVariable String correlationId) {
        List<LogEntry> logs = logService.getLogsByCorrelationId(correlationId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get statistics for a service
     */
    @GetMapping("/stats/{serviceName}")
    public ResponseEntity<Map<String, Long>> getServiceStats(@PathVariable String serviceName) {
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("ERROR", logService.countLogsByServiceAndLevel(serviceName, "ERROR"));
        stats.put("WARN", logService.countLogsByServiceAndLevel(serviceName, "WARN"));
        stats.put("INFO", logService.countLogsByServiceAndLevel(serviceName, "INFO"));
        stats.put("DEBUG", logService.countLogsByServiceAndLevel(serviceName, "DEBUG"));
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "logging-service");
        return ResponseEntity.ok(health);
    }
}
