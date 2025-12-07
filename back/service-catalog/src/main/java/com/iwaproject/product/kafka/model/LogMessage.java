package com.iwaproject.product.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Log message to be sent to Kafka for centralized logging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {
    private String serviceName;
    private String logLevel;
    private String message;
    private LocalDateTime timestamp;
    private String threadName;
    private String loggerName;
    private String correlationId;
    private String userId;
    private String requestUri;
    private String requestMethod;
    private Integer responseStatus;
    private String exceptionMessage;
    private String stackTrace;
}
