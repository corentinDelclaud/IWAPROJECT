package com.iwaproject.logging.model;

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
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "log_level", nullable = false, length = 20)
    private String logLevel;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "thread_name", length = 100)
    private String threadName;

    @Column(name = "logger_name", length = 255)
    private String loggerName;

    @Column(name = "exception_message", columnDefinition = "TEXT")
    private String exceptionMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "request_uri", length = 500)
    private String requestUri;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
