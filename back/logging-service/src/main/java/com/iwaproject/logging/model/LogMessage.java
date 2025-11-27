package com.iwaproject.logging.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for log messages received from Kafka
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {

    private String serviceName;
    private String logLevel;
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    private String threadName;
    private String loggerName;
    private String exceptionMessage;
    private String stackTrace;
    private String correlationId;
    private String userId;
    private String requestUri;
    private String requestMethod;
    private Integer responseStatus;
}
