package com.iwaproject.logging.consumer;

import com.iwaproject.logging.model.LogMessage;
import com.iwaproject.logging.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for log messages
 */
@Component
@Slf4j
public class LogConsumer {

    private final LogService logService;

    public LogConsumer(LogService logService) {
        this.logService = logService;
    }

    /**
     * Consume log messages from Kafka topics
     */
    @KafkaListener(
        topics = {
            "${kafka.topics.logs.auth-service}",
            "${kafka.topics.logs.user-service}",
            "${kafka.topics.logs.catalog-service}",
            "${kafka.topics.logs.api-gateway}",
            "${kafka.topics.logs.stripe-service}"
        },
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLogMessage(
        @Payload LogMessage logMessage,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset
    ) {
        try {
            log.debug("Received log message from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
            
            // Store the log message
            logService.saveLog(logMessage);
            
            // Log error level messages to console for immediate visibility
            if ("ERROR".equals(logMessage.getLogLevel()) || "FATAL".equals(logMessage.getLogLevel())) {
                log.error("Error log from {}: {}", logMessage.getServiceName(), logMessage.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error processing log message from topic {}: {}", topic, e.getMessage(), e);
        }
    }
}
