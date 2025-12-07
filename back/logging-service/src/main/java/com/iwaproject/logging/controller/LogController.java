package com.iwaproject.logging.controller;

import com.iwaproject.logging.model.LogEntry;
import com.iwaproject.logging.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Log Management", description = "APIs for querying and managing application logs from all microservices")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * Get logs by service name
     */
    @Operation(
        summary = "Get logs by service name",
        description = "Retrieve paginated logs filtered by service name with sorting options"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved logs",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping("/service/{serviceName}")
    public ResponseEntity<Page<LogEntry>> getLogsByService(
        @Parameter(description = "Name of the service (e.g., user-microservice)", example = "user-microservice")
        @PathVariable String serviceName,
        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Number of items per page", example = "50")
        @RequestParam(defaultValue = "50") int size,
        @Parameter(description = "Field to sort by", example = "timestamp")
        @RequestParam(defaultValue = "timestamp") String sortBy,
        @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
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
    @Operation(
        summary = "Get logs by log level",
        description = "Retrieve paginated logs filtered by log level (INFO, WARN, ERROR, DEBUG)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved logs"),
        @ApiResponse(responseCode = "400", description = "Invalid log level")
    })
    @GetMapping("/level/{logLevel}")
    public ResponseEntity<Page<LogEntry>> getLogsByLevel(
        @Parameter(description = "Log level to filter by", example = "ERROR")
        @PathVariable String logLevel,
        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Number of items per page", example = "50")
        @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<LogEntry> logs = logService.getLogsByLevel(logLevel.toUpperCase(), pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get error logs
     */
    @Operation(
        summary = "Get all error logs",
        description = "Retrieve paginated logs with ERROR or WARN level for quick troubleshooting"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved error logs")
    @GetMapping("/errors")
    public ResponseEntity<Page<LogEntry>> getErrorLogs(
        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Number of items per page", example = "50")
        @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LogEntry> logs = logService.getErrorLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get logs within a time range
     */
    @Operation(
        summary = "Get logs within a time range",
        description = "Retrieve logs between two specific timestamps. Use ISO 8601 format (e.g., 2025-11-28T10:00:00)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved logs"),
        @ApiResponse(responseCode = "400", description = "Invalid date format or range")
    })
    @GetMapping("/time-range")
    public ResponseEntity<Page<LogEntry>> getLogsByTimeRange(
        @Parameter(description = "Start timestamp (ISO 8601)", example = "2025-11-28T10:00:00")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @Parameter(description = "End timestamp (ISO 8601)", example = "2025-11-28T23:59:59")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Number of items per page", example = "50")
        @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<LogEntry> logs = logService.getLogsByTimeRange(start, end, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get logs by correlation ID
     */
    @Operation(
        summary = "Get logs by correlation ID",
        description = "Retrieve all logs associated with a specific request correlation ID for distributed tracing"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved logs")
    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<List<LogEntry>> getLogsByCorrelationId(
        @Parameter(description = "Correlation ID to trace across services", example = "abc-123-def-456")
        @PathVariable String correlationId
    ) {
        List<LogEntry> logs = logService.getLogsByCorrelationId(correlationId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get statistics for a service
     */
    @Operation(
        summary = "Get log statistics for a service",
        description = "Get count of logs grouped by level (ERROR, WARN, INFO, DEBUG) for a specific service"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    @GetMapping("/stats/{serviceName}")
    public ResponseEntity<Map<String, Long>> getServiceStats(
        @Parameter(description = "Name of the service", example = "user-microservice")
        @PathVariable String serviceName
    ) {
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
    @Operation(
        summary = "Health check",
        description = "Check if the logging service is up and running"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "logging-service");
        return ResponseEntity.ok(health);
    }
}
