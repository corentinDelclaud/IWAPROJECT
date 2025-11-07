package iwaproject.user_microservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event sent by Keycloak when a new user is created
 * This needs to match the structure of events sent by your Keycloak event listener
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeycloakUserEvent {
    private String eventType; // e.g., "REGISTER", "UPDATE", "DELETE"
    private String userId;    // Keycloak user ID (sub)
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Long timestamp;
}
