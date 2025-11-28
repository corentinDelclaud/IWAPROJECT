package iwaproject.user_microservice.kafka.producer;

import iwaproject.user_microservice.kafka.model.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * Kafka producer to publish log messages to centralized logging service
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class LogProducer {

    private final KafkaTemplate<String, LogMessage> kafkaTemplateLog;

    @Value("${spring.application.name:user-microservice}")
    private String serviceName;

    @Value("${kafka.topic.logs:logs-user-service}")
    private String logTopic;

    /**
     * Send a log message to Kafka
     */
    public void sendLog(String level, String message) {
        sendLog(level, message, null, null, null, null, null);
    }

    /**
     * Send a log message with context information
     */
    public void sendLog(String level, String message, String correlationId,
                       String userId, String requestUri, String requestMethod,
                       Integer responseStatus) {

        LogMessage logMessage = LogMessage.builder()
                .serviceName(serviceName)
                .logLevel(level)
                .message(message)
                .timestamp(LocalDateTime.now())
                .threadName(Thread.currentThread().getName())
                .loggerName(getCallerClassName())
                .correlationId(correlationId)
                .userId(userId)
                .requestUri(requestUri)
                .requestMethod(requestMethod)
                .responseStatus(responseStatus)
                .build();

        sendLogMessage(logMessage);
    }

    /**
     * Send an error log with exception details
     */
    public void sendErrorLog(String message, Throwable throwable) {
        sendErrorLog(message, throwable, null, null);
    }

    /**
     * Send an error log with exception and context
     */
    public void sendErrorLog(String message, Throwable throwable, String userId, String correlationId) {
        LogMessage logMessage = LogMessage.builder()
                .serviceName(serviceName)
                .logLevel("ERROR")
                .message(message)
                .timestamp(LocalDateTime.now())
                .threadName(Thread.currentThread().getName())
                .loggerName(getCallerClassName())
                .exceptionMessage(throwable != null ? throwable.getMessage() : null)
                .stackTrace(throwable != null ? getStackTrace(throwable) : null)
                .userId(userId)
                .correlationId(correlationId)
                .build();

        sendLogMessage(logMessage);
    }

    /**
     * Internal method to send log message to Kafka
     */
    private void sendLogMessage(LogMessage logMessage) {
        try {
            kafkaTemplateLog.send(logTopic, logMessage.getCorrelationId(), logMessage)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send log message to Kafka: {}", ex.getMessage());
                        } else {
                            log.trace("Log message sent to Kafka topic: {}", logTopic);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending log to Kafka: {}", e.getMessage());
        }
    }

    /**
     * Get the class name of the caller
     */
    private String getCallerClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // Skip getStackTrace, getCallerClassName, sendLog methods
        if (stackTrace.length > 4) {
            return stackTrace[4].getClassName();
        }
        return "Unknown";
    }

    /**
     * Convert exception stack trace to string
     */
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) return null;

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
