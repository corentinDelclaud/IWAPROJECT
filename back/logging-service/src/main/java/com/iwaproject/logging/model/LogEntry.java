package com.iwaproject.logging.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a log entry
 */
@Entity
@Table(name = "log_entries", indexes = {
    @Index(name = "idx_service_name", columnList = "service_name"),
    @Index(name = "idx_log_level", columnList = "log_level"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Log entry from a microservice")
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the log entry", example = "1")
    private Long id;

    @Column(name = "service_name", nullable = false, length = 100)
    @Schema(description = "Name of the service that generated the log", example = "user-microservice")
    private String serviceName;

    @Column(name = "log_level", nullable = false, length = 20)
    @Schema(description = "Log level (DEBUG, INFO, WARN, ERROR)", example = "INFO")
    private String logLevel;

    @Column(name = "message", columnDefinition = "TEXT")
    @Schema(description = "Log message content", example = "User created successfully")
    private String message;

    @Column(name = "timestamp", nullable = false)
    @Schema(description = "When the log was generated", example = "2025-11-28T13:30:00")
    private LocalDateTime timestamp;

    @Column(name = "thread_name", length = 100)
    @Schema(description = "Name of the thread that generated the log", example = "http-nio-8081-exec-1")
    private String threadName;

    @Column(name = "logger_name", length = 255)
    @Schema(description = "Logger class name", example = "com.iwaproject.UserService")
    private String loggerName;

    @Column(name = "exception_message", columnDefinition = "TEXT")
    @Schema(description = "Exception message if any", example = "NullPointerException")
    private String exceptionMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    @Schema(description = "Full stack trace if exception occurred")
    private String stackTrace;

    @Column(name = "correlation_id", length = 100)
    @Schema(description = "Correlation ID for distributed tracing", example = "abc-123-def-456")
    private String correlationId;

    @Column(name = "user_id", length = 100)
    @Schema(description = "ID of the user associated with this log", example = "user-123")
    private String userId;

    @Column(name = "request_uri", length = 500)
    @Schema(description = "HTTP request URI", example = "/api/users/stats")
    private String requestUri;

    @Column(name = "request_method", length = 10)
    @Schema(description = "HTTP request method", example = "GET")
    private String requestMethod;

    @Column(name = "response_status")
    @Schema(description = "HTTP response status code", example = "200")
    private Integer responseStatus;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "When the log entry was stored in the database", example = "2025-11-28T13:30:01")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
