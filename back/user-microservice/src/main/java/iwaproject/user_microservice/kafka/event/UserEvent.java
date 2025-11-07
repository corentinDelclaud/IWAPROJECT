package iwaproject.user_microservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when user is created, updated, or deleted
 * Other microservices can listen to this topic
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent {
    private String eventType; // "USER_CREATED", "USER_UPDATED", "USER_DELETED"
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime timestamp;
}
