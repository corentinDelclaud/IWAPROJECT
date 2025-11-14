package iwaproject.user_microservice.kafka.producer;

import iwaproject.user_microservice.kafka.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer to publish user events to other microservices
 * Disabled by default - enable with spring.kafka.enabled=true
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class UserEventProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${kafka.topic.user-events}")
    private String userEventsTopic;

    public void publishUserEvent(UserEvent event) {
        log.info("Publishing user event: {} for user: {}", event.getEventType(), event.getUserId());
        
        kafkaTemplate.send(userEventsTopic, event.getUserId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish user event: {}", event, ex);
                    } else {
                        log.info("User event published successfully: {}", event.getEventType());
                    }
                });
    }
}
