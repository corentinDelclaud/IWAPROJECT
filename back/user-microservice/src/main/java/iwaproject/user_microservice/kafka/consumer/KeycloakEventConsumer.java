package iwaproject.user_microservice.kafka.consumer;

import iwaproject.user_microservice.kafka.event.KeycloakUserEvent;
import iwaproject.user_microservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that listens to Keycloak user events
 * When a user registers in Keycloak, this consumer creates the corresponding User entity
 * Disabled by default - enable with spring.kafka.enabled=true
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KeycloakEventConsumer {

    private final UserService userService;

    @KafkaListener(topics = "${kafka.topic.keycloak-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeKeycloakEvent(KeycloakUserEvent event) {
        log.info("Received Keycloak event: {}", event);

        try {
            switch (event.getEventType()) {
                case "REGISTER":
                    handleUserRegistration(event);
                    break;
                case "UPDATE":
                    log.info("User update event received for user: {}", event.getUserId());
                    // You can implement profile sync if needed
                    break;
                case "DELETE":
                    log.info("User delete event received for user: {}", event.getUserId());
                    // You can implement user deletion sync if needed
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing Keycloak event: {}", event, e);
            // In production, you might want to send this to a dead-letter queue
        }
    }

    private void handleUserRegistration(KeycloakUserEvent event) {
        log.info("Creating user from Keycloak registration: {}", event.getUserId());
        
        userService.createUser(
                event.getUserId(),
                event.getUsername(),
                event.getEmail(),
                event.getFirstName(),
                event.getLastName()
        );
        
        log.info("User created successfully from Keycloak event: {}", event.getUserId());
    }
}
